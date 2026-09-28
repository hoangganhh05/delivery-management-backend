package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.ai.*;

import java.util.List;

public interface AiService {

    AiParsedOrderData parseOrder(AiParseOrderRequest request);

    AiChatResponse chat(AiChatRequest request, String currentUsername);

    List<AiShipperRecommendation> recommendShippers(Long orderId);

    Long findBestShipperForOrder(Long orderId);
}
