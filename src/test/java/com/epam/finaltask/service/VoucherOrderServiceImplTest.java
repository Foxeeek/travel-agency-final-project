package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherOrderServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoucherOrderServiceImpl voucherOrderService;

    private VoucherDTO buildVoucherDTO() {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Hello");
        voucherDTO.setDescription("New description");
        voucherDTO.setPrice(850.00);
        voucherDTO.setTourType("ADVENTURE");
        voucherDTO.setHotelType("FIVE_STARS");
        voucherDTO.setStatus("REGISTERED");
        voucherDTO.setTransferType("xxxxx");
        voucherDTO.setArrivalDate(LocalDate.of(2027, 7, 10));
        voucherDTO.setEvictionDate(LocalDate.of(2027, 7, 17));
        voucherDTO.setIsHot(true);
        return voucherDTO;
    }

    private Voucher buildVoucher() {
        Voucher voucher = new Voucher();
        voucher.setTitle("Hello");
        voucher.setDescription("New description");
        voucher.setPrice(BigDecimal.valueOf(850.00));
        voucher.setTourType(TourType.ADVENTURE);
        voucher.setHotelType(HotelType.FIVE_STARS);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setTransferType(TransferType.PLANE);
        voucher.setArrivalDate(LocalDate.of(2027, 7, 10));
        voucher.setEvictionDate(LocalDate.of(2027, 7, 17));
        voucher.setIsHot(true);
        return voucher;
    }

    @Nested
    class OrderTest {
        @Test
        void order_VoucherAndUserExist_LinksUserAndSavesVoucher() {
            UUID voucherId = UUID.randomUUID();
            UUID userid = UUID.randomUUID();

            User user = new User();
            Voucher voucher = buildVoucher();
            VoucherDTO voucherDTO = new VoucherDTO();

            voucher.setId(voucherId);
            user.setId(userid);
            user.setBalance(BigDecimal.valueOf(1000));

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
            when(userRepository.findById(userid)).thenReturn(Optional.of(user));
            when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

            VoucherDTO result = voucherOrderService.order(voucherId, userid);

            ArgumentCaptor<Voucher> captor =
                    ArgumentCaptor.forClass(Voucher.class);

            Assertions.assertSame(voucherDTO, result);
            Assertions.assertEquals(user, voucher.getUser());
            Assertions.assertEquals(0, BigDecimal.valueOf(150).compareTo(user.getBalance()));

            verify(voucherRepository).save(captor.capture());
            verify(userRepository).save(user);

        }

        @Test
        void order_VoucherAlreadyOrdered_ThrowsConflictException() {
            UUID voucherId = UUID.randomUUID();
            UUID userid = UUID.randomUUID();

            Voucher voucher = buildVoucher();
            voucher.setId(voucherId);
            voucher.setUser(new User());

            User user = new User();
            user.setId(userid);
            user.setBalance(BigDecimal.valueOf(1000));

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
            when(userRepository.findById(userid)).thenReturn(Optional.of(user));

            assertThrows(ConflictException.class, () -> voucherOrderService.order(voucherId, userid));

            verify(voucherRepository, never()).save(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void order_VoucherNotRegistered_ThrowsConflictException() {
            UUID voucherId = UUID.randomUUID();
            UUID userid = UUID.randomUUID();

            Voucher voucher = buildVoucher();
            voucher.setId(voucherId);
            voucher.setStatus(VoucherStatus.PAID);

            User user = new User();
            user.setId(userid);
            user.setBalance(BigDecimal.valueOf(1000));

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
            when(userRepository.findById(userid)).thenReturn(Optional.of(user));

            assertThrows(ConflictException.class, () -> voucherOrderService.order(voucherId, userid));

            verify(voucherRepository, never()).save(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void order_InsufficientBalance_ThrowsConflictException() {
            UUID voucherId = UUID.randomUUID();
            UUID userid = UUID.randomUUID();

            Voucher voucher = buildVoucher();
            voucher.setId(voucherId);

            User user = new User();
            user.setId(userid);
            user.setBalance(BigDecimal.valueOf(700));

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
            when(userRepository.findById(userid)).thenReturn(Optional.of(user));

            assertThrows(ConflictException.class, () -> voucherOrderService.order(voucherId, userid));

            verify(voucherRepository, never()).save(any());
            verify(userRepository, never()).save(any());

        }

        @Test
        void order_VoucherNotFound_ThrowsNotFoundException() {
            UUID voucherId = UUID.randomUUID();
            UUID userid = UUID.randomUUID();

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> voucherOrderService.order(voucherId, userid));

            verify(voucherRepository, never()).save(any());
        }

        @Test
        void order_UserNotFound_ThrowsNotFoundException() {
            UUID voucherId = UUID.randomUUID();
            UUID userid = UUID.randomUUID();

            Voucher voucher = new Voucher();
            voucher.setId(voucherId);

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
            when(userRepository.findById(userid)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> voucherOrderService.order(voucherId, userid));

            verify(voucherRepository, never()).save(any());
        }
    }

    @Nested
    class ChangeStatusTest {

        @Test
        void changeStatus_RegisteredWithUser_ToPaid_SavesPaid() {

            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            Voucher voucher = buildVoucher();
            voucher.setId(id);
            voucher.setUser(user);
            VoucherDTO voucherDTO = buildVoucherDTO();
            VoucherStatus status = VoucherStatus.PAID;

            when(voucherRepository.findById(id))
                    .thenReturn(Optional.of(voucher));

            when(voucherMapper.toVoucherDTO(voucher))
                    .thenReturn(voucherDTO);

            VoucherDTO result = voucherOrderService.changeStatus(id, status);

            ArgumentCaptor<Voucher> captor =
                    ArgumentCaptor.forClass(Voucher.class);

            verify(voucherRepository).save(captor.capture());
            verify(userRepository, never()).save(any());

            Assertions.assertSame(voucherDTO, result);
            Assertions.assertEquals(VoucherStatus.PAID, captor.getValue().getStatus());
        }

        @Test
        void changeStatus_RegisteredWithUser_ToCanceled_SavesCanceled() {

            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setBalance(BigDecimal.valueOf(150));
            Voucher voucher = buildVoucher();
            voucher.setId(id);
            voucher.setUser(user);
            VoucherDTO voucherDTO = buildVoucherDTO();
            VoucherStatus status = VoucherStatus.CANCELED;

            when(voucherRepository.findById(id))
                    .thenReturn(Optional.of(voucher));

            when(voucherMapper.toVoucherDTO(voucher))
                    .thenReturn(voucherDTO);

            VoucherDTO result = voucherOrderService.changeStatus(id, status);

            ArgumentCaptor<Voucher> captor =
                    ArgumentCaptor.forClass(Voucher.class);

            verify(voucherRepository).save(captor.capture());

            Assertions.assertSame(voucherDTO, result);
            Assertions.assertEquals(VoucherStatus.CANCELED, captor.getValue().getStatus());
            Assertions.assertEquals(0, BigDecimal.valueOf(1000).compareTo(user.getBalance()));

        }

        @Test
        void changeStatus_VoucherNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();

            VoucherStatus status = VoucherStatus.CANCELED;

            when(voucherRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> voucherOrderService.changeStatus(id, status));

            verify(voucherRepository, never()).save(any());
        }

        @Test
        void changeStatus_VoucherWithoutUser_ThrowsConflictException() {
            UUID id = UUID.randomUUID();

            Voucher voucher = buildVoucher();
            voucher.setId(id);

            VoucherStatus status = VoucherStatus.CANCELED;

            when(voucherRepository.findById(id))
                    .thenReturn(Optional.of(voucher));

            assertThrows(ConflictException.class, () -> voucherOrderService.changeStatus(id, status));

            verify(voucherRepository, never()).save(any());
        }

        @Test
        void changeStatus_AlreadyPaid_ThrowsConflictException() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);

            Voucher voucher = buildVoucher();
            voucher.setStatus(VoucherStatus.PAID);
            voucher.setUser(user);
            voucher.setId(id);

            VoucherStatus status = VoucherStatus.CANCELED;

            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));

            assertThrows(ConflictException.class, () -> voucherOrderService.changeStatus(id, status));

            verify(voucherRepository, never()).save(any());
        }

        @Test
        void changeStatus_ToRegistered_ThrowsConflictException() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);

            Voucher voucher = buildVoucher();
            voucher.setUser(user);
            voucher.setId(id);

            VoucherStatus status = VoucherStatus.REGISTERED;

            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));


            assertThrows(ConflictException.class, () -> voucherOrderService.changeStatus(id, status));

            verify(voucherRepository, never()).save(any());
        }


    }

}
