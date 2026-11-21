package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherStatusRequestDTO;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.VoucherOrderService;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherRestController {
    private final VoucherService voucherService;
    private final VoucherOrderService voucherOrderService;

    public VoucherRestController(VoucherService voucherService, VoucherOrderService voucherOrderService) {
        this.voucherService = voucherService;
        this.voucherOrderService = voucherOrderService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllByUserId(@PathVariable String userId) {
        List<VoucherDTO> vouchers = voucherService.findAllByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> createVoucherValidData(@Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO voucher = voucherService.create(voucherDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("OK", "Voucher is successfully created", voucher));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> changeVoucherStatus(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO voucher = voucherService.changeHotStatus(id, voucherDTO);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher status is successfully changed", voucher));
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> updateVoucher(@PathVariable String id,@Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO voucher = voucherService.update(id, voucherDTO);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher is successfully updated", voucher));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucherById(@PathVariable String id) {
        voucherService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("OK", String.format("Voucher with Id %s has been deleted", id), null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllHotFirst() {
        List<VoucherDTO> vouchers = voucherService.findAllHotFirst();
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @GetMapping("/price")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllByPriceRange(@RequestParam BigDecimal min,@RequestParam BigDecimal max) {
        List<VoucherDTO> vouchers = voucherService.findAllByPriceRange(min, max);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @GetMapping("/user/{userId}/status")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllByUserIdAndStatus(@PathVariable UUID userId, @RequestParam VoucherStatus status) {
        List<VoucherDTO> vouchers = voucherService.findAllByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @GetMapping("/arrival")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllByArrivalPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<VoucherDTO> vouchers = voucherService.findAllByArrivalPeriod(from, to);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @PatchMapping("/{id}/order-status")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> changeOrderStatus(@PathVariable UUID id, @Valid @RequestBody VoucherStatusRequestDTO request) {
        VoucherDTO voucher = voucherOrderService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher status is successfully changed", voucher));
    }

    @PostMapping("/{id}/order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VoucherDTO>> order(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails principal) {
        VoucherDTO voucher = voucherOrderService.order(id, principal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher is successfully ordered", voucher));
    }

}
