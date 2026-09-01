package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.ChangePasswordRequest;
import com.viettel.deliverymanagement.dto.request.UpdateProfileRequest;
import com.viettel.deliverymanagement.dto.request.UpdateUserSettingsRequest;
import com.viettel.deliverymanagement.dto.request.UpsertUserAddressRequest;
import com.viettel.deliverymanagement.dto.response.PasswordChangeResponse;
import com.viettel.deliverymanagement.dto.response.UserAddressResponse;
import com.viettel.deliverymanagement.dto.response.UserMeResponse;
import com.viettel.deliverymanagement.dto.response.UserSettingsResponse;

import java.util.List;

public interface UserService {

    UserMeResponse getCurrentUser(String username);

    UserMeResponse updateProfile(String username, UpdateProfileRequest request);

    PasswordChangeResponse changePassword(String username, ChangePasswordRequest request);

    List<UserAddressResponse> getAddresses(String username);

    UserAddressResponse createAddress(String username, UpsertUserAddressRequest request);

    UserAddressResponse updateAddress(String username, Long addressId, UpsertUserAddressRequest request);

    List<UserAddressResponse> deleteAddress(String username, Long addressId);

    UserAddressResponse setDefaultAddress(String username, Long addressId);

    UserSettingsResponse updateSettings(String username, UpdateUserSettingsRequest request);
}
