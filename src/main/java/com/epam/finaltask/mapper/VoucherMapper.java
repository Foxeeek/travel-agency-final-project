package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.model.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    @Mapping(target = "user", ignore = true)
    Voucher toVoucher(VoucherDTO voucherDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    Voucher toVoucher(VoucherRequestDTO request);

    @Mapping(source = "user.id", target = "userId")
    VoucherDTO toVoucherDTO(Voucher voucher);
}
