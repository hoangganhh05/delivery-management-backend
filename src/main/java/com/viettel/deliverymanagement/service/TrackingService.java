package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.response.TrackingResponse;

public interface TrackingService {

    TrackingResponse trackOrder(String trackingNumber);
}
