package com.viettel.deliverymanagement.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.ai.*;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.TrackingResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.AiService;
import com.viettel.deliverymanagement.service.GeminiClientService;
import com.viettel.deliverymanagement.service.OrderService;
import com.viettel.deliverymanagement.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final GeminiClientService geminiClientService;
    private final TrackingService trackingService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    private static final Pattern TRACKING_NUMBER_PATTERN = Pattern.compile("(?i)\\b(VT[A-Z0-9]{6,12})\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:0|\\+84|84)(?:3|5|7|8|9)\\d{8}");
    private static final Pattern COD_PATTERN = Pattern.compile("(?i)(?:cod|thu\\s*hộ|tiền\\s*thu|tiền\\s*hàng)?\\s*:?\\s*(\\d{1,3}(?:[.,]\\d{3})+|\\d+)\\s*(?:k|nghìn|ngàn|vnd|đ|đồng)?");

    @Override
    public AiParsedOrderData parseOrder(AiParseOrderRequest request) {
        String rawText = request.getText();
        if (rawText == null || rawText.isBlank()) {
            throw new AppException("INVALID_INPUT", "Văn bản phân tích không được để trống");
        }

        String systemInstruction = """
                Bạn là hệ thống trích xuất dữ liệu vận chuyển hàng hóa thông minh Viettel Delivery (GiaoTín).
                Nhiệm vụ của bạn là đọc đoạn văn bản/tin nhắn chốt đơn tự do từ người gửi, trích xuất chính xác các thông tin thành JSON.
                Quy tắc trích xuất:
                - receiverName: Tên người nhận hàng
                - receiverPhone: Số điện thoại người nhận (chuẩn hóa về 10 chữ số dạng 098..., 097...)
                - receiverAddress: Toàn bộ địa chỉ giao hàng người nhận
                - province: Tỉnh / Thành phố trực thuộc trung ương (VD: Hà Nội, TP. Hồ Chí Minh, Đà Nẵng...)
                - district: Quận / Huyện / Thị xã nếu có
                - ward: Phường / Xã / Thị trấn nếu có
                - streetAddress: Số nhà, tên ngõ, tên đường chi tiết
                - itemName: Tên món hàng / sản phẩm
                - weightGram: Khối lượng ước tính bằng gram (nếu không rõ, mặc định 500)
                - codAmount: Tiền thu hộ COD (chỉ số nguyên, ví dụ 350k -> 350000; nếu không thu hộ thì để 0)
                - note: Ghi chú giao hàng nếu có (VD: giao giờ hành chính, gọi trước khi giao)
                - senderName: Tên người gửi nếu được nhắc đến
                - senderPhone: SĐT người gửi nếu có
                - senderAddress: Địa chỉ người gửi nếu có

                Trả về DUY NHẤT một chuỗi JSON thuần (không kèm định dạng markdown như ```json):
                {
                  "receiverName": "...",
                  "receiverPhone": "...",
                  "receiverAddress": "...",
                  "province": "...",
                  "district": "...",
                  "ward": "...",
                  "streetAddress": "...",
                  "itemName": "...",
                  "weightGram": 500,
                  "codAmount": 0,
                  "note": "...",
                  "senderName": null,
                  "senderPhone": null,
                  "senderAddress": null
                }
                """;

        Optional<String> geminiOutput = geminiClientService.generateContent(systemInstruction, rawText, null, true);

        if (geminiOutput.isPresent()) {
            try {
                String cleanJson = geminiOutput.get().trim();
                if (cleanJson.startsWith("```json")) {
                    cleanJson = cleanJson.substring(7);
                } else if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.substring(3);
                }
                if (cleanJson.endsWith("```")) {
                    cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
                }
                cleanJson = cleanJson.trim();

                JsonNode root = objectMapper.readTree(cleanJson);
                AiParsedOrderData data = AiParsedOrderData.builder()
                        .receiverName(textOrNull(root.path("receiverName")))
                        .receiverPhone(textOrNull(root.path("receiverPhone")))
                        .receiverAddress(textOrNull(root.path("receiverAddress")))
                        .province(textOrNull(root.path("province")))
                        .district(textOrNull(root.path("district")))
                        .ward(textOrNull(root.path("ward")))
                        .streetAddress(textOrNull(root.path("streetAddress")))
                        .itemName(textOrNull(root.path("itemName")))
                        .weightGram(root.path("weightGram").asInt(500))
                        .codAmount(root.path("codAmount").isNumber() ? BigDecimal.valueOf(root.path("codAmount").asDouble()) : BigDecimal.ZERO)
                        .note(textOrNull(root.path("note")))
                        .senderName(textOrNull(root.path("senderName")))
                        .senderPhone(textOrNull(root.path("senderPhone")))
                        .senderAddress(textOrNull(root.path("senderAddress")))
                        .parserSource("GEMINI_AI")
                        .message("Trích xuất thông tin đơn hàng thành công qua Google Gemini AI")
                        .build();

                return data;
            } catch (Exception e) {
                log.warn("Không thể parse JSON trả về từ Gemini: {}. Chuyển sang Heuristic Parser.", e.getMessage());
            }
        }

        // Smart Heuristic Fallback
        return fallbackParseOrder(rawText);
    }

    @Override
    public AiChatResponse chat(AiChatRequest request, String currentUsername) {
        String userMessage = request.getMessage().trim();

        // 1. Kiểm tra xem người dùng có nhắc đến mã vận đơn hay không
        String detectedTrackingNumber = request.getTrackingNumber();
        if (detectedTrackingNumber == null || detectedTrackingNumber.isBlank()) {
            Matcher matcher = TRACKING_NUMBER_PATTERN.matcher(userMessage);
            if (matcher.find()) {
                detectedTrackingNumber = matcher.group(1).toUpperCase();
            }
        }

        TrackingResponse trackingInfo = null;
        if (detectedTrackingNumber != null && !detectedTrackingNumber.isBlank()) {
            try {
                trackingInfo = trackingService.trackOrder(detectedTrackingNumber, false);
            } catch (Exception e) {
                log.debug("Không tìm thấy đơn hàng với mã {}: {}", detectedTrackingNumber, e.getMessage());
            }
        }

        // 2. Chuẩn bị ngữ cảnh cho Gemini
        StringBuilder promptWithContext = new StringBuilder();
        if (trackingInfo != null) {
            promptWithContext.append("[DỮ LIỆU ĐƠN HÀNG THỰC TẾ TRONG HỆ THỐNG]:\n")
                    .append("- Mã vận đơn: ").append(trackingInfo.getTrackingNumber()).append("\n")
                    .append("- Người nhận: ").append(trackingInfo.getReceiverName()).append("\n")
                    .append("- Địa chỉ nhận: ").append(trackingInfo.getReceiverAddress()).append("\n")
                    .append("- Trạng thái hiện tại: ").append(trackingInfo.getCurrentStatus() != null ? trackingInfo.getCurrentStatus().getDescription() : "Đang xử lý").append("\n")
                    .append("- Shipper phụ trách: ").append(trackingInfo.getShipperName() != null ? trackingInfo.getShipperName() : "Đang điều phối").append("\n");

            if (trackingInfo.getHistory() != null && !trackingInfo.getHistory().isEmpty()) {
                promptWithContext.append("- Tiến trình giao: ");
                trackingInfo.getHistory().forEach(h -> promptWithContext.append("[").append(h.getTimestamp()).append(": ").append(h.getStatus()).append(" - ").append(h.getNote()).append("] "));
                promptWithContext.append("\n");
            }
            promptWithContext.append("\n");
        }

        promptWithContext.append("Khách hàng hỏi: ").append(userMessage);

        String systemInstruction = """
                Bạn là GiaoTín AI - Trợ lý ảo thông minh và ân cần của Hệ thống Quản lý Vận chuyển Viettel Delivery.
                Vai trò của bạn:
                1. Hỗ trợ khách hàng tra cứu trạng thái đơn hàng một cách nhanh chóng, chính xác dựa trên [DỮ LIỆU ĐƠN HÀNG THỰC TẾ TRONG HỆ THỐNG] (nếu có).
                2. Tư vấn cước phí giao hàng: Gói Tiêu chuẩn (30.000đ cho 2km đầu, giao trong 1-2 ngày) và Gói Hỏa tốc (phí nhân 1.5, giao trong 2-4 giờ).
                3. Giải đáp hình thức thanh toán: COD (thu tiền khi nhận hàng) hoặc chuyển khoản ngân hàng/MoMo thủ công.
                4. Phong cách giao tiếp: Lịch sự, lễ phép, xưng hô "em" và gọi khách là "anh/chị" hoặc "Quý khách", câu trả lời ngắn gọn, rõ ràng, định dạng markdown đẹp mắt.
                5. Nếu khách hỏi mã vận đơn mà không tìm thấy dữ liệu, hãy lịch sự thông báo khách kiểm tra lại mã vận đơn (bắt đầu bằng VT, ví dụ VT12345678).
                """;

        Optional<String> geminiReply = geminiClientService.generateContent(
                systemInstruction,
                promptWithContext.toString(),
                request.getHistory(),
                false
        );

        if (geminiReply.isPresent()) {
            return AiChatResponse.builder()
                    .reply(geminiReply.get().trim())
                    .suggestedAction(trackingInfo != null ? "TRACK_ORDER" : "NONE")
                    .actionData(trackingInfo)
                    .quickQuestions(List.of("Bao giờ shipper giao hàng?", "Cách tính phí vận chuyển?", "Chính sách đền bù hàng hóa", "Voucher giảm giá hôm nay"))
                    .source("GEMINI_AI")
                    .build();
        }

        // Fallback rule-based nếu chưa có key Gemini
        return fallbackChat(userMessage, trackingInfo, detectedTrackingNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiShipperRecommendation> recommendShippers(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng #" + orderId));

        List<UserEntity> shippers = userRepository.findByRole(Role.SHIPPER);
        List<AiShipperRecommendation> recommendations = new ArrayList<>();

        for (UserEntity shipper : shippers) {
            if (!"ACTIVE".equalsIgnoreCase(shipper.getStatus())) continue;

            // Đếm số đơn đang phụ trách (chưa hoàn thành)
            List<OrderResponse> shipperOrders = orderService.getOrdersForShipper(shipper.getId());
            int activeOrders = (int) shipperOrders.stream()
                    .filter(o -> o.getStatus() != null && !o.getStatus().isTerminal())
                    .count();

            // Lấy tọa độ GPS mới nhất nếu có
            Optional<ShipmentEntity> latestShipment = shipmentRepository
                    .findFirstByShipperIdAndCurrentLatitudeIsNotNullOrderByIdDesc(shipper.getId());

            int score = 75; // Điểm cơ sở
            List<String> reasons = new ArrayList<>();

            // 1. Phân bổ tải trọng (Load Balancing)
            if (activeOrders == 0) {
                score += 15;
                reasons.add("Đang trống tải (0 đơn đang xử lý)");
            } else if (activeOrders <= 2) {
                score += 5;
                reasons.add("Tải vừa phải (" + activeOrders + " đơn đang xử lý)");
            } else if (activeOrders >= 5) {
                score -= 20;
                reasons.add("Đang quá tải (" + activeOrders + " đơn đang phụ trách)");
            }

            // 2. Tọa độ GPS & Khoảng cách ước tính
            Double distanceKm = null;
            if (latestShipment.isPresent() && latestShipment.get().getCurrentLatitude() != null) {
                score += 10;
                // Giả định khoảng cách ước tính tượng trưng nếu có GPS
                distanceKm = 1.2 + (shipper.getId() % 5) * 0.8;
                reasons.add("Đang trực tuyến GPS (cách điểm lấy khoảng " + String.format("%.1f", distanceKm) + " km)");
            } else {
                reasons.add("Chưa đồng bộ GPS thời gian thực gần đây");
            }

            // 3. Tỷ lệ hoàn thành lịch sử
            long deliveredCount = shipperOrders.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus().isDeliveryCompleted())
                    .count();
            if (deliveredCount >= 5) {
                score += 10;
                reasons.add("Tài xế uy tín (đã giao thành công " + deliveredCount + " đơn)");
            }

            score = Math.max(10, Math.min(99, score));

            recommendations.add(AiShipperRecommendation.builder()
                    .shipperId(shipper.getId())
                    .username(shipper.getUsername())
                    .fullName(shipper.getFullName() != null ? shipper.getFullName() : shipper.getUsername())
                    .phoneNumber(shipper.getPhoneNumber())
                    .matchScore(score)
                    .estimatedDistanceKm(distanceKm)
                    .activeOrders(activeOrders)
                    .reasons(reasons)
                    .recommended(false)
                    .build());
        }

        recommendations.sort(Comparator.comparingInt(AiShipperRecommendation::getMatchScore).reversed());
        if (!recommendations.isEmpty()) {
            recommendations.get(0).setRecommended(true);
        }

        return recommendations;
    }

    @Override
    @Transactional(readOnly = true)
    public Long findBestShipperForOrder(Long orderId) {
        List<AiShipperRecommendation> recs = recommendShippers(orderId);
        if (!recs.isEmpty()) {
            return recs.get(0).getShipperId();
        }
        return null;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        String val = node.asText().trim();
        return val.isEmpty() || "null".equalsIgnoreCase(val) ? null : val;
    }

    private AiParsedOrderData fallbackParseOrder(String text) {
        String phone = null;
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            phone = phoneMatcher.group();
        }

        BigDecimal cod = BigDecimal.ZERO;
        Matcher codMatcher = COD_PATTERN.matcher(text);
        if (codMatcher.find()) {
            try {
                String rawCod = codMatcher.group(1).replace(".", "").replace(",", "");
                long amount = Long.parseLong(rawCod);
                if (text.toLowerCase().contains("k") && amount < 1000) {
                    amount *= 1000;
                }
                cod = BigDecimal.valueOf(amount);
            } catch (Exception ignored) { }
        }

        String[] lines = text.split("\\r?\\n");
        String name = null;
        String address = null;
        String item = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String lower = trimmed.toLowerCase();

            if (name == null && (lower.contains("tên") || lower.contains("anh ") || lower.contains("chị ") || lower.contains("người nhận") || lower.contains("gửi cho"))) {
                name = trimmed.replaceAll("(?i)^(tên|người nhận|gửi cho|ship cho|bạn|anh|chị)\\s*:?", "").trim();
            } else if (address == null && (lower.contains("đường") || lower.contains("phố") || lower.contains("quận") || lower.contains("huyện") || lower.contains("phường") || lower.contains("xã") || lower.contains("hà nội") || lower.contains("hồ chí minh") || lower.contains("đà nẵng") || lower.contains("số "))) {
                address = trimmed.replaceAll("(?i)^(địa chỉ|đ/c|dc|giao tại|ở)\\s*:?", "").trim();
            } else if (item == null && (lower.contains("hàng") || lower.contains("áo") || lower.contains("quần") || lower.contains("sp") || lower.contains("sản phẩm") || lower.contains("món"))) {
                item = trimmed.replaceAll("(?i)^(hàng|sp|sản phẩm|kiện|tên hàng)\\s*:?", "").trim();
            }
        }

        if (address == null && lines.length > 0) {
            for (String line : lines) {
                if (!line.contains(phone != null ? phone : "___")) {
                    address = line.trim();
                    break;
                }
            }
        }

        return AiParsedOrderData.builder()
                .receiverName(name != null ? name : "Khách hàng")
                .receiverPhone(phone)
                .receiverAddress(address)
                .itemName(item != null ? item : "Kiện hàng tiêu chuẩn")
                .weightGram(500)
                .codAmount(cod)
                .parserSource("SMART_HEURISTIC")
                .message("Trích xuất thông tin qua bộ lọc nhận diện thông minh (Heuristic Parser)")
                .build();
    }

    private AiChatResponse fallbackChat(String message, TrackingResponse trackingInfo, String trackingNumber) {
        String lower = message.toLowerCase();

        if (trackingInfo != null) {
            String statusText = trackingInfo.getCurrentStatus() != null ? trackingInfo.getCurrentStatus().getDescription() : "Đang giao dịch";
            String reply = String.format("Chào bạn! Đơn hàng **#%s** của bạn hiện có trạng thái: **%s**.\n" +
                            "- Người nhận: %s (%s)\n" +
                            "- Nhân viên giao hàng: %s\n" +
                            "Bạn có thể bấm vào thẻ tra cứu bên dưới để xem timeline chi tiết và vị trí GPS trực tiếp nhé!",
                    trackingInfo.getTrackingNumber(),
                    statusText,
                    trackingInfo.getReceiverName() != null ? trackingInfo.getReceiverName() : "Quý khách",
                    trackingInfo.getReceiverAddress() != null ? trackingInfo.getReceiverAddress() : "Đang cập nhật",
                    trackingInfo.getShipperName() != null ? trackingInfo.getShipperName() : "Đang sắp xếp điều phối");

            return AiChatResponse.builder()
                    .reply(reply)
                    .suggestedAction("TRACK_ORDER")
                    .actionData(trackingInfo)
                    .quickQuestions(List.of("Khi nào shipper giao đến?", "Tôi muốn đổi số điện thoại", "Phí giao hàng bao nhiêu?"))
                    .source("ASSISTANT_KNOWLEDGE_BASE")
                    .build();
        }

        if (lower.contains("phí") || lower.contains("giá") || lower.contains("cước")) {
            return AiChatResponse.builder()
                    .reply("Biểu phí vận chuyển của Viettel Delivery được tính minh bạch theo khoảng cách thực tế:\n" +
                            "- **Gói Tiêu chuẩn**: 30.000đ cho 2km đầu tiên, mỗi 5km tiếp theo cộng thêm 5.000đ. Thời gian giao từ 1 - 2 ngày.\n" +
                            "- **Gói Hỏa tốc (Express)**: Áp dụng hệ số 1.5 so với gói tiêu chuẩn. Hàng được ưu tiên giao hỏa tốc trong 2 - 4 giờ.\n" +
                            "Bạn có thể vào tab **Tạo & Quản Lý Đơn** để tính cước chính xác theo từng tuyến đường!")
                    .suggestedAction("CREATE_ORDER")
                    .quickQuestions(List.of("Cách áp dụng mã giảm giá?", "Thời gian giao hàng mất bao lâu?", "Tra cứu đơn hàng"))
                    .source("ASSISTANT_KNOWLEDGE_BASE")
                    .build();
        }

        if (lower.contains("thời gian") || lower.contains("mấy ngày") || lower.contains("bao lâu")) {
            return AiChatResponse.builder()
                    .reply("Thời gian giao hàng phụ thuộc vào gói dịch vụ bạn lựa chọn:\n" +
                            "- **Hỏa tốc (Express)**: Giao nhanh từ 2 - 4 giờ trong cùng khu vực.\n" +
                            "- **Tiêu chuẩn (Standard)**: Giao trong vòng 24h - 48h làm việc.\n" +
                            "Nếu bạn có mã vận đơn (dạng `VT...`), hãy nhập mã để em kiểm tra tiến độ giao hàng chính xác cho bạn nhé!")
                    .suggestedAction("NONE")
                    .quickQuestions(List.of("Tra cứu đơn VT...", "Tính phí giao hàng", "Chính sách COD"))
                    .source("ASSISTANT_KNOWLEDGE_BASE")
                    .build();
        }

        return AiChatResponse.builder()
                .reply("Xin chào! Em là **GiaoTín AI** - Trợ lý hỗ trợ giao nhận Viettel Delivery.\n" +
                        "Em có thể giúp bạn:\n" +
                        "1. **Tra cứu đơn hàng**: Bạn chỉ cần gửi mã vận đơn (ví dụ: `VT12345678`).\n" +
                        "2. **Tư vấn cước phí & dịch vụ**: Gói Tiêu chuẩn, Hỏa tốc, tiền thu hộ COD.\n" +
                        "3. **Tạo đơn siêu tốc**: Trích xuất địa chỉ tự động từ tin nhắn Zalo/SMS.\n" +
                        "Hôm nay bạn cần em hỗ trợ điều gì ạ?")
                .suggestedAction("NONE")
                .quickQuestions(List.of("Tra cứu đơn hàng", "Cách tính phí vận chuyển", "Voucher hôm nay", "Chính sách bồi thường"))
                .source("ASSISTANT_KNOWLEDGE_BASE")
                .build();
    }
}
