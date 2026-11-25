package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.VoucherStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappersTest {

    private final VoucherMapper voucherMapper = Mappers.getMapper(VoucherMapper.class);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private Voucher voucher(User owner, boolean hot) {
        Voucher voucher = new Voucher();
        voucher.setId(UUID.randomUUID());
        voucher.setTitle("Rome weekend");
        voucher.setDescription("Colosseum");
        voucher.setPrice(new BigDecimal("480.50"));
        voucher.setTourType(TourType.CULTURAL);
        voucher.setTransferType(TransferType.PLANE);
        voucher.setHotelType(HotelType.FOUR_STARS);
        voucher.setStatus(VoucherStatus.PAID);
        voucher.setArrivalDate(LocalDate.of(2027, 3, 5));
        voucher.setEvictionDate(LocalDate.of(2027, 3, 8));
        voucher.setIsHot(hot);
        voucher.setUser(owner);
        return voucher;
    }

    private User user(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setPassword("hash");
        user.setRole(role);
        user.setPhoneNumber("+380501112233");
        user.setBalance(new BigDecimal("123.45"));
        user.setActive(true);
        return user;
    }

    @Nested
    class VoucherMapperTest {

        @Test
        void toVoucherDTO_OrderedVoucher_CopiesAllFieldsAndOwnerId() {
            User owner = user(Role.USER);
            Voucher voucher = voucher(owner, true);

            VoucherDTO dto = voucherMapper.toVoucherDTO(voucher);

            assertEquals(voucher.getId().toString(), dto.getId());
            assertEquals("Rome weekend", dto.getTitle());
            assertEquals("Colosseum", dto.getDescription());
            assertEquals(480.50, dto.getPrice());
            assertEquals("CULTURAL", dto.getTourType());
            assertEquals("PLANE", dto.getTransferType());
            assertEquals("FOUR_STARS", dto.getHotelType());
            assertEquals("PAID", dto.getStatus());
            assertEquals(LocalDate.of(2027, 3, 5), dto.getArrivalDate());
            assertEquals(LocalDate.of(2027, 3, 8), dto.getEvictionDate());
            assertTrue(dto.getIsHot());
            assertEquals(owner.getId(), dto.getUserId());
        }

        @Test
        void toVoucherDTO_FreeVoucher_HasNoOwnerId() {
            VoucherDTO dto = voucherMapper.toVoucherDTO(voucher(null, false));

            assertNull(dto.getUserId());
            assertFalse(dto.getIsHot());
        }

        @Test
        void toVoucher_FromDto_ParsesEnumsAndPriceAndIgnoresOwner() {
            VoucherDTO dto = voucherMapper.toVoucherDTO(voucher(user(Role.USER), true));

            Voucher voucher = voucherMapper.toVoucher(dto);

            assertEquals(TourType.CULTURAL, voucher.getTourType());
            assertEquals(TransferType.PLANE, voucher.getTransferType());
            assertEquals(HotelType.FOUR_STARS, voucher.getHotelType());
            assertEquals(VoucherStatus.PAID, voucher.getStatus());
            assertEquals(0, new BigDecimal("480.5").compareTo(voucher.getPrice()));
            assertTrue(voucher.getIsHot());
            assertNull(voucher.getUser());
        }

        @Test
        void toVoucher_FromRequest_IgnoresIdOwnerAndStatus() {
            VoucherRequestDTO request = new VoucherRequestDTO();
            request.setTitle("New tour");
            request.setDescription("Fresh");
            request.setPrice(99.99);
            request.setTourType(TourType.ECO);
            request.setTransferType(TransferType.BUS);
            request.setHotelType(HotelType.ONE_STAR);
            request.setArrivalDate(LocalDate.of(2027, 5, 1));
            request.setEvictionDate(LocalDate.of(2027, 5, 5));
            request.setIsHot(true);

            Voucher voucher = voucherMapper.toVoucher(request);

            assertEquals("New tour", voucher.getTitle());
            assertEquals(0, new BigDecimal("99.99").compareTo(voucher.getPrice()));
            assertEquals(TourType.ECO, voucher.getTourType());
            assertEquals(TransferType.BUS, voucher.getTransferType());
            assertEquals(HotelType.ONE_STAR, voucher.getHotelType());
            assertEquals(LocalDate.of(2027, 5, 5), voucher.getEvictionDate());
            assertTrue(voucher.getIsHot());
            assertNull(voucher.getId());
            assertNull(voucher.getUser());
            assertNull(voucher.getStatus());
        }

        @Test
        void nullInputs_ReturnNull() {
            assertNull(voucherMapper.toVoucherDTO(null));
            assertNull(voucherMapper.toVoucher((VoucherDTO) null));
            assertNull(voucherMapper.toVoucher((VoucherRequestDTO) null));
        }
    }

    @Nested
    class UserMapperTest {

        @Test
        void toUserResponseDTO_CopiesPublicFieldsWithoutPassword() {
            User user = user(Role.MANAGER);

            UserResponseDTO dto = userMapper.toUserResponseDTO(user);

            assertEquals(user.getId().toString(), dto.getId());
            assertEquals("john_doe", dto.getUsername());
            assertEquals("MANAGER", dto.getRole());
            assertEquals("+380501112233", dto.getPhoneNumber());
            assertEquals(123.45, dto.getBalance());
            assertTrue(dto.isActive());
        }

        @Test
        void toUserDTO_CopiesFields() {
            User user = user(Role.ADMIN);

            UserDTO dto = userMapper.toUserDTO(user);

            assertEquals(user.getId().toString(), dto.getId());
            assertEquals("john_doe", dto.getUsername());
            assertEquals("ADMIN", dto.getRole());
            assertEquals("+380501112233", dto.getPhoneNumber());
            assertEquals(123.45, dto.getBalance());
            assertTrue(dto.isActive());
        }

        @Test
        void toUser_FromDto_ParsesRoleBalanceAndId() {
            UUID id = UUID.randomUUID();
            UserDTO dto = new UserDTO();
            dto.setId(id.toString());
            dto.setUsername("jane_doe");
            dto.setPassword("hash");
            dto.setRole("USER");
            dto.setPhoneNumber("+380509998877");
            dto.setBalance(50.0);
            dto.setActive(true);

            User user = userMapper.toUser(dto);

            assertEquals(id, user.getId());
            assertEquals("jane_doe", user.getUsername());
            assertEquals("hash", user.getPassword());
            assertEquals(Role.USER, user.getRole());
            assertEquals("+380509998877", user.getPhoneNumber());
            assertEquals(0, new BigDecimal("50.0").compareTo(user.getBalance()));
            assertTrue(user.isActive());
        }

        @Test
        void toUser_NewUserWithoutId_KeepsIdNull() {
            UserDTO dto = new UserDTO();
            dto.setUsername("jane_doe");
            dto.setPassword("hash");
            dto.setRole("USER");
            dto.setBalance(0.0);

            assertNull(userMapper.toUser(dto).getId());
        }

        @Test
        void toVoucher_FromRequest_IgnoresIdOwnerAndStatus() {
            VoucherRequestDTO request = new VoucherRequestDTO();
            request.setTitle("T");
            request.setPrice(10.0);

            Voucher voucher = userMapper.toVoucher(request);

            assertEquals("T", voucher.getTitle());
            assertNull(voucher.getId());
            assertNull(voucher.getUser());
            assertNull(voucher.getStatus());
        }

        @Test
        void nullInputs_ReturnNull() {
            assertNull(userMapper.toUser(null));
            assertNull(userMapper.toUserDTO(null));
            assertNull(userMapper.toUserResponseDTO(null));
            assertNull(userMapper.toVoucher(null));
        }
    }
}
