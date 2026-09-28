package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.ai.*;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Smart Features", description = "Các API tính năng thông minh: Parse tin nhắn tạo đơn, Chatbot tư vấn, Điều phối Shipper bằng AI")
public class AiController {

    private final AiService aiService;

    @PostMapping("/parse-order")
    @Operation(summary = "Tự động trích xuất thông tin người nhận, địa chỉ, hàng hóa từ tin nhắn chốt đơn (NLP/LLM)")
    public ResponseData<AiParsedOrderData> parseOrder(@Valid @RequestBody AiParseOrderRequest request) {
        AiParsedOrderData parsed = aiService.parseOrder(request);
        return ResponseData.success("Phân tích thông tin đơn hàng thành công", parsed);
    }

    @PostMapping("/chat")
    @Operation(summary = "Trợ lý ảo chăm sóc khách hàng và tra cứu hành trình đơn hàng tự động (RAG / Tool Grounding)")
    public ResponseData<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        AiChatResponse response = aiService.chat(request, username);
        return ResponseData.success("Phản hồi từ trợ lý ảo AI", response);
    }

    @GetMapping("/recommend-shippers/{orderId}")
    @Operation(summary = "Gợi ý danh sách shipper tối ưu nhất cho đơn hàng dựa trên điểm số AI (GPS, tải trọng, độ uy tín)")
    @PreAuthorize("@permissionService.has(authentication, 'DISPATCH_ORDERS')")
    public ResponseData<List<AiShipperRecommendation>> recommendShippers(@PathVariable Long orderId) {
        List<AiShipperRecommendation> recommendations = aiService.recommendShippers(orderId);
        return ResponseData.success("Lấy danh sách đề xuất shipper thành công", recommendations);
    }
}
