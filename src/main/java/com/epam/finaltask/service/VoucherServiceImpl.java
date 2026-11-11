package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherMapper voucherMapper;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    public VoucherServiceImpl(VoucherMapper voucherMapper, VoucherRepository voucherRepository, UserRepository userRepository) {
        this.voucherMapper = voucherMapper;
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        voucherRepository.save(voucher);
        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Voucher " + id + " not found"));
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        voucher.setUser(user);
        voucherRepository.save(voucher);
        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {

        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Voucher " + id + " not found"));
        voucher.setTitle(voucherDTO.getTitle());
        voucher.setDescription(voucherDTO.getDescription());

        voucher.setPrice(BigDecimal.valueOf(voucherDTO.getPrice()));
        voucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        voucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        voucher.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));
        voucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        voucher.setArrivalDate(voucherDTO.getArrivalDate());
        voucher.setEvictionDate(voucherDTO.getEvictionDate());

        voucher.setIsHot(voucherDTO.getIsHot());

        voucherRepository.save(voucher);
        return voucherMapper.toVoucherDTO(voucher);

    }

    @Override
    public void delete(String voucherId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(voucherId))
                .orElseThrow(() -> new NotFoundException("Voucher " + voucherId + " not found"));
        voucherRepository.delete(voucher);
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Voucher " + id + " not found"));
        Voucher fromDto = voucherMapper.toVoucher(voucherDTO);

        voucher.setIsHot(fromDto.getIsHot());
        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);

    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        return voucherRepository.findAllByUserId(UUID.fromString(userId)).stream()
                .map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        return voucherRepository.findAllByTourType(tourType).stream()
                .map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        return voucherRepository.findAllByTransferType(TransferType.valueOf(transferType)).stream()
                .map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        return voucherRepository.findAllByPrice(price).stream()
                .map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        return voucherRepository.findAllByHotelType(hotelType).stream()
                .map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll().stream()
                .map(voucherMapper::toVoucherDTO).toList();
    }
}
