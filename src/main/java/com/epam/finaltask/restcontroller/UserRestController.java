package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.*;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserService userService;
    private final VoucherService voucherService;

    public UserRestController(UserService userService, VoucherService voucherService) {
        this.userService = userService;
        this.voucherService = voucherService;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changeUserStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusRequestDTO request) {
        UserResponseDTO result = userService.changeAccountStatus(id, request.getActive());
        return ResponseEntity.ok(new ApiResponse<>("OK", request.getActive() ? "User is successfully unblocked" : "User is successfully blocked", result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        UserResponseDTO user = userService.getProfile(principal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Profile is successfully found", user));
    }

    @GetMapping("/me/vouchers")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getMyVouchers(@AuthenticationPrincipal CustomUserDetails principal) {
        List<VoucherDTO> vouchers = voucherService.findAllByUserId(principal.getUser().getId().toString());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody UserUpdateRequestDTO request) {
        UserResponseDTO user = userService.updateProfile(principal.getUser().getId(), request);

        return ResponseEntity.ok(new ApiResponse<>("OK", "Profile is successfully updated", user));
    }

}
