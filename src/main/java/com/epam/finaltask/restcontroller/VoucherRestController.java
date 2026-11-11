package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherRestController {
    private final VoucherService voucherService;

    public VoucherRestController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAll() {
        List<VoucherDTO> vouchers = voucherService.findAll();
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllByUserId(@PathVariable String userId) {
        List<VoucherDTO> vouchers = voucherService.findAllByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Vouchers are successfully found", vouchers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherDTO>> createVoucherValidData(@Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO voucher = voucherService.create(voucherDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("OK", "Voucher is successfully created", voucher));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VoucherDTO>> changeVoucherStatus(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO voucher = voucherService.changeHotStatus(id, voucherDTO);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher status is successfully changed", voucher));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherDTO>> updateVoucher(@PathVariable String id,@Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO voucher = voucherService.update(id, voucherDTO);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher is successfully updated", voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucherById(@PathVariable String id) {
        voucherService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("OK", String.format("Voucher with Id %s has been deleted", id), null));
    }

}
