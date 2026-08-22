package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;

public interface ShipmentService {

    void assignShipper(AssignShipperRequest request);

    void updateShipmentStatus(Long orderId, UpdateShipmentStatusRequest request);
}
