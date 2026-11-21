package com.epam.finaltask.service;


import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class VoucherOrderServiceImpl implements VoucherOrderService{

    private final VoucherMapper voucherMapper;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    public VoucherOrderServiceImpl(UserRepository userRepository, VoucherMapper voucherMapper, VoucherRepository voucherRepository) {
        this.userRepository = userRepository;
        this.voucherMapper = voucherMapper;
        this.voucherRepository = voucherRepository;
    }

    @Override
    @Transactional
    public VoucherDTO order(UUID id, UUID userId) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> {
                            log.warn("Voucher with id {} not found", id);
                            return new NotFoundException("Voucher " + id + " not found");
                        }
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                            log.warn("User with id {} not found", userId);
                            return new NotFoundException("User " + userId + " not found");
                        }
                );


        if (voucher.getUser() != null){
            log.warn("Voucher {} is already ordered by another user", id);
            throw new ConflictException("Voucher " + id + " is already ordered");
        }
        if (voucher.getStatus() != VoucherStatus.REGISTERED){
            log.warn("Voucher {} has status {}, only REGISTERED can be ordered", id, voucher.getStatus());
            throw new ConflictException("Only REGISTERED voucher can be ordered");
        }
        if (user.getBalance().compareTo(voucher.getPrice()) < 0){
            log.warn("User {} has insufficient balance for voucher {}", userId, id);
            throw new ConflictException("Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(voucher.getPrice()));
        userRepository.save(user);

        voucher.setUser(user);
        voucherRepository.save(voucher);

        log.info("User {} ordered voucher {}", user.getUsername(), voucher.getTitle());
        return voucherMapper.toVoucherDTO(voucher);
    }


    @Override
    public VoucherDTO changeStatus(UUID id, VoucherStatus status) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> {
                            log.warn("Voucher with id {} not found", id);
                            return new NotFoundException("Voucher " + id + " not found");
                        }
                );

        if (voucher.getUser() == null){
            log.warn("Voucher {} has no user, status cannot be changed", id);
            throw new ConflictException("Voucher " + id + " has no user");
        }
        if (voucher.getStatus() != VoucherStatus.REGISTERED){
            log.warn("Voucher {} has status {}, only REGISTERED can be changed", id, voucher.getStatus());
            throw new ConflictException("Only REGISTERED voucher can change its status");
        }
        if (!status.equals(VoucherStatus.PAID) && !status.equals(VoucherStatus.CANCELED)){
            log.warn("Voucher {} cannot be moved to status {}", id, status);
            throw new ConflictException("Status can only be changed to PAID or CANCELED");
        }
        voucher.setStatus(status);
        voucherRepository.save(voucher);

        log.info("Voucher {} status changed to {}", voucher.getId(), voucher.getStatus());

        return voucherMapper.toVoucherDTO(voucher);

    }
}
