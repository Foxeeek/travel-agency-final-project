package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.info("Voucher created successfully with ID: {}", voucher.getId());
        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow( () -> {
                            log.warn("Voucher with id {} not found", id);
                            return new NotFoundException("Voucher " + id + " not found");
                        }
                );

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow( () -> {
                            log.warn("User with id {} not found", userId);
                            return new NotFoundException("User " + userId + " not found");
                        }
                );

        voucher.setUser(user);
        voucherRepository.save(voucher);

        log.info("User {} ordered voucher {}", user.getUsername(), voucher.getTitle());
        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {

        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow( () -> {
                            log.warn("Voucher with id {} not found", id);
                            return new NotFoundException("Voucher " + id + " not found");
                        }
                );
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

        log.info("Voucher {} updated successfully", voucher.getId());

        return voucherMapper.toVoucherDTO(voucher);

    }

    @Override
    public void delete(String voucherId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(voucherId))
                .orElseThrow( () -> {
                            log.warn("Voucher with id {} not found", voucherId);
                            return new NotFoundException("Voucher " + voucherId + " not found");
                        }
                );
        voucherRepository.delete(voucher);
        log.info("Voucher deleted successfully with ID: {}", voucher.getId());
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow( () -> {
                            log.warn("Voucher with id {} not found", id);
                            return new NotFoundException("Voucher " + id + " not found");
                        }
                );
        Voucher fromDto = voucherMapper.toVoucher(voucherDTO);

        voucher.setIsHot(fromDto.getIsHot());
        voucherRepository.save(voucher);

        log.info("Voucher {} hot flag set to {}", voucher.getId(), voucher.getIsHot());

        return voucherMapper.toVoucherDTO(voucher);

    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        List<VoucherDTO> vouchers = voucherRepository.findAllByUserId(UUID.fromString(userId)).stream()
                .map(voucherMapper::toVoucherDTO).toList();
        log.debug("Found {} vouchers by user id {}", vouchers.size(), userId);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        List<VoucherDTO> vouchers = voucherRepository.findAllByTourType(tourType).stream()
                .map(voucherMapper::toVoucherDTO).toList();
        log.debug("Found {} vouchers by tour type {}", vouchers.size(), tourType);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        List<VoucherDTO> vouchers = voucherRepository.findAllByTransferType(TransferType.valueOf(transferType)).stream()
                .map(voucherMapper::toVoucherDTO).toList();
        log.debug("Found {} vouchers by transfer type {}", vouchers.size(), transferType);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        List<VoucherDTO> vouchers = voucherRepository.findAllByPrice(price).stream()
                .map(voucherMapper::toVoucherDTO).toList();
        log.debug("Found {} vouchers by price {}", vouchers.size(), price);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        List<VoucherDTO> vouchers = voucherRepository.findAllByHotelType(hotelType).stream()
                .map(voucherMapper::toVoucherDTO).toList();
        log.debug("Found {} vouchers by hotel type {}", vouchers.size(), hotelType);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAll() {
        List<VoucherDTO> vouchers = voucherRepository.findAll().stream()
                .map(voucherMapper::toVoucherDTO).toList();
        log.debug("Found {} vouchers", vouchers.size());
        return vouchers;
    }
}
