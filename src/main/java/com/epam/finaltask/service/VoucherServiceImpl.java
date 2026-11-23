package com.epam.finaltask.service;

import com.epam.finaltask.dto.PageResponseDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.VoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class VoucherServiceImpl implements VoucherService {

    private static final Set<String> SORT_FIELDS = Set.of("price", "title", "arrivalDate", "evictionDate", "tourType", "hotelType", "transferType", "status");
    private final VoucherMapper voucherMapper;
    private final VoucherRepository voucherRepository;

    public VoucherServiceImpl(VoucherMapper voucherMapper, VoucherRepository voucherRepository) {
        this.voucherMapper = voucherMapper;
        this.voucherRepository = voucherRepository;
    }

    private Voucher voucherById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> {
                            log.warn("Voucher with id {} not found", id);
                            return new NotFoundException("Voucher " + id + " not found","err.voucher.notFound",id);
                        }
                );
    }

    private List<VoucherDTO> toDtoList(List<Voucher> vouchers) {
        return vouchers.stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public VoucherDTO create(VoucherRequestDTO request) {
        Voucher voucher = voucherMapper.toVoucher(request);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucherRepository.save(voucher);

        log.info("Voucher created successfully with ID: {}", voucher.getId());
        return voucherMapper.toVoucherDTO(voucher);
    }


    @Override
    public VoucherDTO update(String id, VoucherRequestDTO request) {

        Voucher voucher = voucherById(UUID.fromString(id));
        voucher.setTitle(request.getTitle());
        voucher.setDescription(request.getDescription());

        voucher.setPrice(BigDecimal.valueOf(request.getPrice()));
        voucher.setTourType(request.getTourType());
        voucher.setHotelType(request.getHotelType());
        voucher.setTransferType(request.getTransferType());
        voucher.setArrivalDate(request.getArrivalDate());
        voucher.setEvictionDate(request.getEvictionDate());

        voucher.setIsHot(request.getIsHot());
        voucherRepository.save(voucher);

        log.info("Voucher {} updated successfully", voucher.getId());

        return voucherMapper.toVoucherDTO(voucher);

    }

    @Override
    public void delete(String voucherId) {
        Voucher voucher = voucherById(UUID.fromString(voucherId));
        if (voucher.getUser() != null) {
            log.warn("Voucher {} is ordered and cannot be deleted", voucher.getId());
            throw new ConflictException("Voucher is ordered and cannot be deleted", "err.voucher.orderedCannotDelete");
        }
        voucherRepository.delete(voucher);
        log.info("Voucher deleted successfully with ID: {}", voucher.getId());
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherById(UUID.fromString(id));
        Voucher fromDto = voucherMapper.toVoucher(voucherDTO);

        voucher.setIsHot(fromDto.getIsHot());
        voucherRepository.save(voucher);

        log.info("Voucher {} hot flag set to {}", voucher.getId(), voucher.getIsHot());

        return voucherMapper.toVoucherDTO(voucher);

    }

    @Override
    public VoucherDTO findById(String id) {
        Voucher voucher = voucherById(UUID.fromString(id));
        log.debug("Found voucher by id {}", id);
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
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllByTourType(tourType));
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
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllByPrice(price));
        log.debug("Found {} vouchers by price {}", vouchers.size(), price);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllByHotelType(hotelType));
        log.debug("Found {} vouchers by hotel type {}", vouchers.size(), hotelType);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAll() {
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAll());
        log.debug("Found {} vouchers", vouchers.size());
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllHotFirst() {
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllHotFirst());
        log.debug("Found {} vouchers", vouchers.size());
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByPriceRange(BigDecimal min, BigDecimal max) {
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllByPriceRange(min, max));
        log.debug("Found {} vouchers with price between {} : {}", vouchers.size(), min, max);
        return vouchers;

    }

    @Override
    public List<VoucherDTO> findAllByUserIdAndStatus(UUID userId, VoucherStatus status) {
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllByUserIdAndStatus(userId, status));
        log.debug("Found {} vouchers for user {} with {} status", vouchers.size(), userId, status);
        return vouchers;
    }

    @Override
    public List<VoucherDTO> findAllByArrivalPeriod(LocalDate from, LocalDate to) {
        List<VoucherDTO> vouchers = toDtoList(voucherRepository.findAllByArrivalPeriod(from, to));
        log.debug("Found {} vouchers for period from {} to {}", vouchers.size(), from, to);
        return vouchers;
    }


    @Override
    public PageResponseDTO<VoucherDTO> search(VoucherFilterDTO filter, Pageable pageable) {
        Specification<Voucher> specification = VoucherSpecifications.fromFilter(filter);
        checkSortFields(pageable.getSort());
        Sort sort = Sort.by(Sort.Order.desc("isHot")).and(pageable.getSort());
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Voucher> page = voucherRepository.findAll(specification, sorted);
        Page<VoucherDTO> dtoPage = page.map(voucherMapper::toVoucherDTO);

        log.debug("Filtered");

        return new PageResponseDTO<>(dtoPage);
    }

    private void checkSortFields(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                log.warn("Unsupported sort field: {}", order.getProperty());
                throw new IllegalArgumentException("Unsupported sort field: " + order.getProperty());
            }
        }
    }

}
