package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentLocationRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;
import com.viettel.deliverymanagement.dto.response.DriverLocationResponse;
import com.viettel.deliverymanagement.dto.response.ShipmentOfferResponse;
import java.util.List;

public interface ShipmentService {

    void assignShipper(AssignShipperRequest request);
    void autoAssign(Long orderId);
    List<ShipmentOfferResponse> getPendingOffers(String username);
    void acceptOffer(Long offerId, String username);
    void declineOffer(Long offerId, String username);

    void updateShipmentStatus(Long orderId, UpdateShipmentStatusRequest request, String username);

    void updateShipmentLocation(Long orderId, UpdateShipmentLocationRequest request, String username);

    DriverLocationResponse getLatestLocation(Long orderId);
}
