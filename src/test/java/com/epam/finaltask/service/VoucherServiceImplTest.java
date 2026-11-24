package com.epam.finaltask.service;


import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.dto.PageResponseDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @InjectMocks
    private VoucherServiceImpl voucherService;

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

    private VoucherRequestDTO buildVoucherRequest() {
        VoucherRequestDTO request = new VoucherRequestDTO();
        request.setTitle("Hello");
        request.setDescription("New description");
        request.setPrice(850.00);
        request.setTourType(TourType.ADVENTURE);
        request.setHotelType(HotelType.FIVE_STARS);
        request.setTransferType(TransferType.PLANE);

        request.setArrivalDate(LocalDate.of(2027, 7, 10));
        request.setEvictionDate(LocalDate.of(2027, 7, 17));
        request.setIsHot(true);

        return request;
    }

    @Test
    void create_ValidRequest_SetsRegisteredStatusSavesAndReturnsDto() {
        UUID id = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(id);

        VoucherRequestDTO request = buildVoucherRequest();
        VoucherDTO voucherDTO = buildVoucherDTO();

        when(voucherMapper.toVoucher(request)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.create(request);

        Assertions.assertSame(voucherDTO, result);
        Assertions.assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());

        verify(voucherMapper).toVoucher(request);
        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Nested
    class UpdateTest {

        @Test
        void update_VoucherNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();
            VoucherRequestDTO request = buildVoucherRequest();

            when(voucherRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> voucherService.update(id.toString(), request));

            verify(voucherRepository, never()).save(any());
        }

        @Test
        void update_VoucherOrderedByUser_ThrowsConflictExceptionAndDoesNotSave() {
            UUID id = UUID.randomUUID();
            Voucher voucher = new Voucher();
            voucher.setId(id);
            voucher.setTitle("Original");
            voucher.setUser(new User());
            VoucherRequestDTO request = buildVoucherRequest();

            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));

            ConflictException ex = assertThrows(ConflictException.class, () -> voucherService.update(id.toString(), request));

            Assertions.assertEquals("err.voucher.orderedCannotUpdate", ex.getKey());
            Assertions.assertEquals(0, ex.getArgs().length);
            Assertions.assertEquals("Original", voucher.getTitle());
            verify(voucherRepository, never()).save(any());
            verifyNoInteractions(voucherMapper);
        }

        @Test
        void update_ExistingVoucher_UpdatesAllFieldsAndSaves() {
            UUID id = UUID.randomUUID();
            Voucher voucher = new Voucher();
            voucher.setId(id);
            VoucherRequestDTO request = buildVoucherRequest();
            VoucherDTO voucherDTO = buildVoucherDTO();

            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));
            when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

            VoucherDTO result = voucherService.update(id.toString(), request);

            ArgumentCaptor<Voucher> captor = ArgumentCaptor.forClass(Voucher.class);
            verify(voucherRepository).save(captor.capture());

            Assertions.assertSame(voucherDTO, result);
            Assertions.assertEquals("Hello", captor.getValue().getTitle());
            Assertions.assertEquals("New description", captor.getValue().getDescription());
            Assertions.assertEquals(0, new BigDecimal("850.00").compareTo(captor.getValue().getPrice()));
            Assertions.assertEquals(TourType.ADVENTURE, captor.getValue().getTourType());
            Assertions.assertEquals(HotelType.FIVE_STARS, captor.getValue().getHotelType());
            Assertions.assertEquals(TransferType.PLANE, captor.getValue().getTransferType());
            Assertions.assertEquals(LocalDate.of(2027, 7, 10), captor.getValue().getArrivalDate());
            Assertions.assertEquals(LocalDate.of(2027, 7, 17), captor.getValue().getEvictionDate());
            Assertions.assertTrue(captor.getValue().getIsHot());
        }

        @Test
        void update_FreeVoucher_DoesNotChangeStatusAndStaysWithoutOwner() {
            UUID id = UUID.randomUUID();
            Voucher voucher = new Voucher();
            voucher.setId(id);
            voucher.setStatus(VoucherStatus.REGISTERED);

            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));
            when(voucherMapper.toVoucherDTO(voucher)).thenReturn(buildVoucherDTO());

            voucherService.update(id.toString(), buildVoucherRequest());

            Assertions.assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());
            Assertions.assertNull(voucher.getUser());
        }
    }

    @Nested
    class DeleteTest {
        @Test
        void delete_ExistingVoucher_DeletesVoucher() {
            UUID voucherId = UUID.randomUUID();
            Voucher voucher = new Voucher();

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

            voucherService.delete(voucherId.toString());

            Assertions.assertFalse(voucherRepository.existsById(voucherId));
            verify(voucherRepository).delete(voucher);
        }

        @Test
        void delete_VoucherNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();
            VoucherDTO voucherDTO = new VoucherDTO();

            when(voucherRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> voucherService.delete(id.toString()));
            verify(voucherRepository, never()).delete(any(Voucher.class));
        }

        @Test
        void delete_VoucherOrderedByUser_ThrowsConflictExceptionAndKeepsVoucher() {
            UUID voucherId = UUID.randomUUID();
            Voucher voucher = new Voucher();
            voucher.setId(voucherId);
            voucher.setUser(new User());

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

            ConflictException ex = Assertions.assertThrows(ConflictException.class, () -> voucherService.delete(voucherId.toString()));

            Assertions.assertEquals("err.voucher.orderedCannotDelete", ex.getKey());

            Assertions.assertEquals(0, ex.getArgs().length);

            verify(voucherRepository, never()).delete(any(Voucher.class));
        }
    }

    @Nested
    class FindByIdTest {

        @Test
        void findById_ExistingVoucher_ReturnsMappedDto() {
            UUID id = UUID.randomUUID();
            Voucher voucher = new Voucher();
            voucher.setId(id);
            VoucherDTO dto = buildVoucherDTO();

            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));
            when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

            VoucherDTO result = voucherService.findById(id.toString());

            Assertions.assertSame(dto, result);
            verify(voucherRepository).findById(id);
        }

        @Test
        void findById_VoucherNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();

            when(voucherRepository.findById(id)).thenReturn(Optional.empty());

            NotFoundException ex = Assertions.assertThrows(NotFoundException.class, () -> voucherService.findById(id.toString()));

            Assertions.assertEquals("err.voucher.notFound", ex.getKey());

            Assertions.assertArrayEquals(new Object[]{id}, ex.getArgs());

            verifyNoInteractions(voucherMapper);
        }
    }

    @Nested
    class ChangeHotStatusTest {
        @Test
        void changeHotStatus_ExistingVoucher_SavesNewHotFlag() {
            UUID voucherId = UUID.randomUUID();
            VoucherDTO voucherDTO = buildVoucherDTO();
            voucherDTO.setId(voucherId.toString());

            Voucher voucher = new Voucher();
            voucher.setId(voucherId);

            Voucher updatedVoucher = new Voucher();
            updatedVoucher.setId(voucherId);
            updatedVoucher.setIsHot(true);

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
            when(voucherMapper.toVoucher(voucherDTO)).thenReturn(updatedVoucher);
            when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

            ArgumentCaptor<Voucher> captor =
                    ArgumentCaptor.forClass(Voucher.class);

            VoucherDTO result = voucherService.changeHotStatus(voucherId.toString(), voucherDTO);

            Assertions.assertNotNull(result);

            verify(voucherRepository).save(captor.capture());

            Assertions.assertTrue(voucher.getIsHot());
        }

        @Test
        void changeHotStatus_VoucherNotFound_ThrowsNotFoundException() {
            UUID voucherId = UUID.randomUUID();
            VoucherDTO voucherDTO = buildVoucherDTO();

            when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> voucherService.changeHotStatus(voucherId.toString(), voucherDTO));

            verify(voucherRepository, never()).save(any());
        }
    }

    @Nested
    class FindAllTest {
        @Test
        void findAll_TwoVouchersExist_ReturnsTwoDtos() {
            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAll()).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAll();
            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAll();
        }

        @Test
        void findAllByUserId_UserHasTwoVouchers_ReturnsTwoDtos() {
            UUID userId = UUID.randomUUID();
            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByUserId(userId)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAllByUserId(userId.toString());
            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByUserId(userId);
        }

        @Test
        void findAllByTourType_TwoVouchersMatch_ReturnsTwoDtos() {
            TourType tourType = TourType.ADVENTURE;

            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByTourType(tourType)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAllByTourType(tourType);
            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByTourType(tourType);
        }

        @Test
        void findAllByTransferType_TwoVouchersMatch_ReturnsTwoDtos() {
            TransferType transferType = TransferType.PLANE;

            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByTransferType(transferType)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAllByTransferType(transferType.toString());
            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByTransferType(transferType);
        }

        @Test
        void findAllByPrice_TwoVouchersMatch_ReturnsTwoDtos() {
            Double price = 850.00;

            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByPrice(price)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAllByPrice(price);
            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByPrice(price);
        }

        @Test
        void findAllByHotelType_TwoVouchersMatch_ReturnsTwoDtos() {
            HotelType hotelType = HotelType.FIVE_STARS;

            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByHotelType(hotelType)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAllByHotelType(hotelType);
            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByHotelType(hotelType);
        }

        @Test
        void findAllHotFirst_VouchersExist_ReturnsDtosInRepositoryOrder() {
            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();
            Voucher voucher3 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);
            vouchers.add(voucher3);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();
            VoucherDTO dto3 = new VoucherDTO();

            when(voucherRepository.findAllHotFirst()).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);
            when(voucherMapper.toVoucherDTO(voucher3)).thenReturn(dto3);

            List<VoucherDTO> result = voucherService.findAllHotFirst();

            Assertions.assertEquals(List.of(dto1, dto2, dto3), result);

            verify(voucherRepository).findAllHotFirst();
        }

        @Test
        void findAllByPriceRange_VouchersInRange_ReturnsMatchingDtos() {
            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();
            Voucher voucher3 = buildVoucher();

            voucher1.setPrice(new BigDecimal(500));
            voucher2.setPrice(new BigDecimal(405));


            vouchers.add(voucher1);
            vouchers.add(voucher2);


            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();


            when(voucherRepository.findAllByPriceRange(new BigDecimal(400), new BigDecimal(1000))).thenReturn(List.of(voucher1, voucher2));
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

            List<VoucherDTO> result = voucherService.findAllByPriceRange(new BigDecimal(400), new BigDecimal(1000));

            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByPriceRange(new BigDecimal(400), new BigDecimal(1000));
        }

        @Test
        void findAllByUserIdAndStatus_UserHasPaidVouchers_ReturnsDtos() {
            UUID userId = UUID.randomUUID();

            VoucherStatus status = VoucherStatus.PAID;

            List<Voucher> vouchers = new ArrayList<>();

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            User user1 = new User();
            user1.setId(userId);

            voucher1.setUser(user1);
            voucher2.setUser(user1);

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByUserIdAndStatus(userId, status)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);


            List<VoucherDTO> result = voucherService.findAllByUserIdAndStatus(userId, status);

            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByUserIdAndStatus(userId, status);
        }

        @Test
        void findAllByArrivalPeriod_VouchersInPeriod_ReturnsMatchingDtos() {
            List<Voucher> vouchers = new ArrayList<>();

            LocalDate from = LocalDate.of(2026, 12, 10);
            LocalDate to = LocalDate.of(2026, 12, 19);

            Voucher voucher1 = buildVoucher();
            Voucher voucher2 = buildVoucher();

            vouchers.add(voucher1);
            vouchers.add(voucher2);

            VoucherDTO dto1 = new VoucherDTO();
            VoucherDTO dto2 = new VoucherDTO();

            when(voucherRepository.findAllByArrivalPeriod(from, to)).thenReturn(vouchers);
            when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
            when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);


            List<VoucherDTO> result = voucherService.findAllByArrivalPeriod(from, to);

            Assertions.assertEquals(List.of(dto1, dto2), result);

            verify(voucherRepository).findAllByArrivalPeriod(from, to);
        }
    }


    @Nested
    @SuppressWarnings("unchecked")
    class SearchTest {

        @Test
        void search_ValidSort_ReturnsPageAndSortsHotFirst() {
            VoucherFilterDTO filter = new VoucherFilterDTO();
            Pageable requested = PageRequest.of(1, 5, Sort.by("price"));
            Voucher voucher = buildVoucher();
            VoucherDTO dto = buildVoucherDTO();
            Page<Voucher> page = new PageImpl<>(List.of(voucher), PageRequest.of(1, 5), 6);

            when(voucherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(voucherMapper.toVoucherDTO(voucher)).thenReturn(dto);

            PageResponseDTO<VoucherDTO> result = voucherService.search(filter, requested);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(voucherRepository).findAll(any(Specification.class), captor.capture());

            Pageable used = captor.getValue();
            Assertions.assertEquals(1, used.getPageNumber());
            Assertions.assertEquals(5, used.getPageSize());
            Sort.Order first = used.getSort().iterator().next();
            Assertions.assertEquals("isHot", first.getProperty());
            Assertions.assertEquals(Sort.Direction.DESC, first.getDirection());
            Assertions.assertNotNull(used.getSort().getOrderFor("price"));

            Assertions.assertEquals(List.of(dto), result.getContent());
            Assertions.assertEquals(1, result.getPage());
            Assertions.assertEquals(5, result.getSize());
            Assertions.assertEquals(6, result.getTotalElements());
            Assertions.assertEquals(2, result.getTotalPages());
        }

        @Test
        void search_NoSortRequested_SortsOnlyByHotFlag() {
            Page<Voucher> page = new PageImpl<>(List.of());

            when(voucherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            voucherService.search(new VoucherFilterDTO(), PageRequest.of(0, 10));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(voucherRepository).findAll(any(Specification.class), captor.capture());

            List<Sort.Order> orders = captor.getValue().getSort().toList();
            Assertions.assertEquals(1, orders.size());
            Assertions.assertEquals("isHot", orders.get(0).getProperty());
        }

        @Test
        void search_UnsupportedSortField_ThrowsIllegalArgumentException() {
            Pageable requested = PageRequest.of(0, 10, Sort.by("user.password"));

            assertThrows(IllegalArgumentException.class, () -> voucherService.search(new VoucherFilterDTO(), requested));

            verify(voucherRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}
