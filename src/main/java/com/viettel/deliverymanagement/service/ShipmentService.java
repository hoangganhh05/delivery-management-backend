package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentLocationRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;
import com.viettel.deliverymanagement.dto.response.DriverLocationResponse;

public interface ShipmentService {

    void assignShipper(AssignShipperRequest request);

    void updateShipmentStatus(Long orderId, UpdateShipmentStatusRequest request, String username);

    void updateShipmentLocation(Long orderId, UpdateShipmentLocationRequest request, String username);

    DriverLocationResponse getLatestLocation(Long orderId);
}
