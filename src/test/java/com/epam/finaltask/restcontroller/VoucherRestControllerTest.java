package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.dto.PageResponseDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.model.*;
import com.epam.finaltask.service.VoucherOrderService;
import com.epam.finaltask.service.VoucherService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class VoucherRestControllerTest {

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private VoucherOrderService voucherOrderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void findAllHotFirst_Successfully() throws Exception {
        List<VoucherDTO> voucherDTOList = new ArrayList<>();
        when(voucherService.findAllHotFirst()).thenReturn(voucherDTOList);
        MvcResult result = mockMvc.perform(get("/api/vouchers"))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");

        List<VoucherDTO> responseVoucherDTOList = objectMapper.convertValue(resultsNode, new TypeReference<List<VoucherDTO>>() {
        });
        assertEquals(voucherDTOList, responseVoucherDTOList);
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void findAllByUserId_Successfully() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());
        List<VoucherDTO> voucherDTOList = new ArrayList<>();

        when(voucherService.findAllByUserId(userId)).thenReturn(voucherDTOList);

        MvcResult result = mockMvc.perform(get("/api/vouchers/user/" + userId))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");

        List<VoucherDTO> responseVoucherDTOList = objectMapper.convertValue(resultsNode, new TypeReference<List<VoucherDTO>>() {
        });

        assertEquals(voucherDTOList, responseVoucherDTOList);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createVoucher_ValidData_SuccessfullyCreated() throws Exception {
        VoucherRequestDTO voucherDTO = new VoucherRequestDTO();
        voucherDTO.setTitle("SummerSale2024");
        voucherDTO.setDescription("Summer Sale Voucher Description");
        voucherDTO.setPrice(299.99);
        voucherDTO.setTourType(TourType.ADVENTURE);
        voucherDTO.setTransferType(TransferType.PLANE);
        voucherDTO.setHotelType(HotelType.FIVE_STARS);
        voucherDTO.setArrivalDate(LocalDate.of(2024, 6, 15));
        voucherDTO.setEvictionDate(LocalDate.of(2024, 6, 20));
        voucherDTO.setIsHot(false);

        String expectedStatusCode = "OK";
        String expectedMessage = "Voucher is successfully created";

        when(voucherService.create(any(VoucherRequestDTO.class))).thenReturn(new VoucherDTO());

        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(voucherDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateVoucher_ValidData_SuccessfullyUpdated() throws Exception {
        VoucherRequestDTO voucherDTO = new VoucherRequestDTO();
        voucherDTO.setTitle("UpdatedTitle");
        voucherDTO.setDescription("Updated description");
        voucherDTO.setPrice(499.99);
        voucherDTO.setTourType(TourType.SAFARI);
        voucherDTO.setTransferType(TransferType.JEEPS);
        voucherDTO.setHotelType(HotelType.THREE_STARS);
        voucherDTO.setArrivalDate(LocalDate.of(2024, 7, 15));
        voucherDTO.setEvictionDate(LocalDate.of(2024, 7, 20));
        voucherDTO.setIsHot(true);

        String voucherId = String.valueOf(UUID.randomUUID());
        String expectedStatusCode = "OK";
        String expectedMessage = "Voucher is successfully updated";

        when(voucherService.update(eq(voucherId), any(VoucherRequestDTO.class))).thenReturn(new VoucherDTO());

        mockMvc.perform(patch("/api/vouchers/" + voucherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(voucherDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteVoucherById_VoucherExists_SuccessfullyDeleted() throws Exception {
        String voucherId = String.valueOf(UUID.randomUUID());
        String expectedStatusCode = "OK";
        String expectedMessage = String.format("Voucher with Id %s has been deleted", voucherId);

        doNothing().when(voucherService).delete(voucherId);

        mockMvc.perform(delete("/api/vouchers/" + voucherId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

        verify(voucherService, times(1)).delete(voucherId);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "ROLE_MANAGER"})
    void changeVoucherStatus_ValidData_SuccessfullyChanged() throws Exception {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setIsHot(true);

        String voucherId = String.valueOf(UUID.randomUUID());
        String expectedStatusCode = "OK";
        String expectedMessage = "Voucher status is successfully changed";

        when(voucherService.changeHotStatus(eq(voucherId), any(VoucherDTO.class))).thenReturn(voucherDTO);

        mockMvc.perform(patch("/api/vouchers/" + voucherId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(voucherDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

        verify(voucherService, times(1)).changeHotStatus(eq(voucherId), any(VoucherDTO.class));
    }

    @Test
    @WithMockUser
    void findAllByPriceRange_Successfully() throws Exception {
        List<VoucherDTO> voucherDTOList = new ArrayList<>();
        BigDecimal min = new BigDecimal(200);
        BigDecimal max = new BigDecimal(1000);

        when(voucherService.findAllByPriceRange(min, max)).thenReturn(voucherDTOList);

        MvcResult result = mockMvc.perform(get("/api/vouchers/price").param("min", min.toString()).param("max", max.toString()))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");

        List<VoucherDTO> responseVoucherDTOList = objectMapper.convertValue(resultsNode, new TypeReference<List<VoucherDTO>>() {
        });

        assertEquals(voucherDTOList, responseVoucherDTOList);

        verify(voucherService).findAllByPriceRange(min, max);
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void findAllByUserIdAndStatus_Successfully() throws Exception {
        UUID id = UUID.randomUUID();
        List<VoucherDTO> voucherDTOList = new ArrayList<>();
        VoucherStatus status = VoucherStatus.PAID;

        when(voucherService.findAllByUserIdAndStatus(id, status)).thenReturn(voucherDTOList);

        MvcResult result = mockMvc.perform(get("/api/vouchers/user/" + id + "/status").param("status", status.toString()))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");

        List<VoucherDTO> responseVoucherDTOList = objectMapper.convertValue(resultsNode, new TypeReference<List<VoucherDTO>>() {
        });

        assertEquals(voucherDTOList, responseVoucherDTOList);

        verify(voucherService).findAllByUserIdAndStatus(id, status);
    }

    @Test
    @WithMockUser
    void findAllByArrivalPeriod() throws Exception {
        List<VoucherDTO> voucherDTOList = new ArrayList<>();
        LocalDate from = LocalDate.of(2027, 11, 25);
        LocalDate to = LocalDate.of(2027, 12, 12);

        when(voucherService.findAllByArrivalPeriod(from, to)).thenReturn(voucherDTOList);

        MvcResult result = mockMvc.perform(get("/api/vouchers/arrival").param("from", from.toString()).param("to", to.toString()))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");

        List<VoucherDTO> responseVoucherDTOList = objectMapper.convertValue(resultsNode, new TypeReference<List<VoucherDTO>>() {
        });

        assertEquals(voucherDTOList, responseVoucherDTOList);

        verify(voucherService).findAllByArrivalPeriod(from, to);
    }

    @Nested
    @WithMockUser(authorities = "ROLE_ADMIN")
    class ExceptionErrorTest {


        @Test
        void createVoucher_MissingTitle_Returns400() throws Exception {
            VoucherRequestDTO voucherDTO = new VoucherRequestDTO();
            voucherDTO.setDescription("Summer Sale Voucher Description");
            voucherDTO.setPrice(299.99);
            voucherDTO.setTourType(TourType.ADVENTURE);
            voucherDTO.setTransferType(TransferType.PLANE);
            voucherDTO.setHotelType(HotelType.FIVE_STARS);
            voucherDTO.setArrivalDate(LocalDate.of(2024, 6, 15));
            voucherDTO.setEvictionDate(LocalDate.of(2024, 6, 20));
            voucherDTO.setIsHot(false);

            String expectedStatusCode = "BAD_REQUEST";

            mockMvc.perform(post("/api/vouchers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(voucherDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value(containsString("title")));
            verifyNoInteractions(voucherService);
        }

        @Test
        void createVoucher_NegativePrice_Returns400() throws Exception {
            VoucherRequestDTO voucherDTO = new VoucherRequestDTO();
            voucherDTO.setTitle("Voucher title");
            voucherDTO.setDescription("Summer Sale Voucher Description");
            voucherDTO.setPrice(-299.99);
            voucherDTO.setTourType(TourType.ADVENTURE);
            voucherDTO.setTransferType(TransferType.PLANE);
            voucherDTO.setHotelType(HotelType.FIVE_STARS);
            voucherDTO.setArrivalDate(LocalDate.of(2024, 6, 15));
            voucherDTO.setEvictionDate(LocalDate.of(2024, 6, 20));
            voucherDTO.setIsHot(false);

            String expectedStatusCode = "BAD_REQUEST";

            mockMvc.perform(post("/api/vouchers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(voucherDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value(containsString("price")));
            verifyNoInteractions(voucherService);
        }

        @Test
        void updateVoucher_NotFound_Returns404() throws Exception {
            VoucherRequestDTO voucherDTO = new VoucherRequestDTO();
            UUID id = UUID.randomUUID();
            voucherDTO.setTitle("Voucher title");
            voucherDTO.setDescription("Summer Sale Voucher Description");
            voucherDTO.setPrice(299.99);
            voucherDTO.setTourType(TourType.ADVENTURE);
            voucherDTO.setTransferType(TransferType.PLANE);
            voucherDTO.setHotelType(HotelType.FIVE_STARS);
            voucherDTO.setArrivalDate(LocalDate.of(2024, 6, 15));
            voucherDTO.setEvictionDate(LocalDate.of(2024, 6, 20));
            voucherDTO.setIsHot(false);

            String expectedStatusCode = "NOT_FOUND";
            String expectedMessage = "Voucher not found";

            when(voucherService.update(any(), any())).thenThrow(new NotFoundException(expectedMessage));

            mockMvc.perform(patch("/api/vouchers/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(voucherDTO)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

            verify(voucherService).update(eq(id.toString()), any(VoucherRequestDTO.class));
        }

        @Test
        void deleteVoucher_NotFound_Returns404() throws Exception {
            UUID id = UUID.randomUUID();

            String expectedStatusCode = "NOT_FOUND";
            String expectedMessage = "Voucher not found";

            doThrow(new NotFoundException(expectedMessage)).when(voucherService).delete(id.toString());

            mockMvc.perform(delete("/api/vouchers/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

            verify(voucherService).delete(id.toString());
        }


        @Test
        void findAllByPriceRange_MissingParams_Returns400() throws Exception {
            String expectedStatusCode = "BAD_REQUEST";

            mockMvc.perform(get("/api/vouchers/price"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value(containsString("min")));

            verifyNoInteractions(voucherService);
        }

        @Test
        void findAllByPriceRange_InvalidMinType_Returns400() throws Exception {
            String expectedStatusCode = "BAD_REQUEST";

            mockMvc.perform(get("/api/vouchers/price?min=abc&max=1000"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value("Required parameter min has invalid value: 'abc'"));

            verifyNoInteractions(voucherService);
        }

        @Test
        void unknownUrl_Returns404() throws Exception {

            String expectedStatusCode = "NOT_FOUND";
            String expectedMessage = "Resource not found";

            mockMvc.perform(patch("/api/no-such-path"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                    .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

            verifyNoInteractions(voucherService);
        }


    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void changeOrderStatus_ValidData_Returns200() throws Exception {
        UUID id = UUID.randomUUID();

        String expectedStatusCode = "OK";
        String expectedMessage = "Voucher status is successfully changed";

        VoucherDTO dto = new VoucherDTO();

        when(voucherOrderService.changeStatus(id, VoucherStatus.PAID)).thenReturn(dto);

        mockMvc.perform(patch("/api/vouchers/{id}/order-status", id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

        verify(voucherOrderService).changeStatus(id, VoucherStatus.PAID);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void changeOrderStatus_AsUser_Returns403() throws Exception {
        UUID id = UUID.randomUUID();

        String expectedStatusCode = "FORBIDDEN";
        String expectedMessage = "Access denied";

        mockMvc.perform(patch("/api/vouchers/{id}/order-status", id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

        verifyNoInteractions(voucherOrderService);


    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void changeOrderStatus_InvalidStatus_Returns400() throws Exception {
        UUID id = UUID.randomUUID();

        String expectedStatusCode = "BAD_REQUEST";
        String expectedMessage = "Malformed request body";

        VoucherDTO dto = new VoucherDTO();

        mockMvc.perform(patch("/api/vouchers/{id}/order-status", id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"FOO\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(expectedStatusCode))
                .andExpect(jsonPath("$.statusMessage").value(expectedMessage));

        verifyNoInteractions(voucherOrderService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void changeOrderStatus_Conflict_Returns409() throws Exception {
        UUID id = UUID.randomUUID();

        when(voucherOrderService.changeStatus(id, VoucherStatus.PAID)).thenThrow(new ConflictException("Voucher has no user"));

        mockMvc.perform(patch("/api/vouchers/{id}/order-status", id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusMessage").value("Voucher has no user"))
                .andExpect(jsonPath("$.statusCode").value("CONFLICT"));

        verify(voucherOrderService).changeStatus(id, VoucherStatus.PAID);
    }


    private CustomUserDetails customUser(UUID id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setActive(true);
        return new CustomUserDetails(user);
    }

    @Test
    void order_AsUser_Returns200() throws Exception {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(voucherOrderService.order(voucherId, userId)).thenReturn(new VoucherDTO());

        mockMvc.perform(post("/api/vouchers/{id}/order", voucherId)
                        .with(user(customUser(userId, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Voucher is successfully ordered"));

        verify(voucherOrderService).order(voucherId, userId);
    }

    @Test
    void order_AsManager_Returns403() throws Exception {
        UUID voucherId = UUID.randomUUID();

        mockMvc.perform(post("/api/vouchers/{id}/order", voucherId)
                        .with(user(customUser(UUID.randomUUID(), Role.MANAGER))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value("FORBIDDEN"))
                .andExpect(jsonPath("$.statusMessage").value("Access denied"));

        verifyNoInteractions(voucherOrderService);
    }

    @Test
    void order_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/vouchers/{id}/order", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.statusMessage").value("Authentication required"));

        verifyNoInteractions(voucherOrderService);
    }

    @Test
    void order_InsufficientBalance_Returns409() throws Exception {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(voucherOrderService.order(voucherId, userId)).thenThrow(new ConflictException("Insufficient balance"));

        mockMvc.perform(post("/api/vouchers/{id}/order", voucherId)
                        .with(user(customUser(userId, Role.USER))))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value("CONFLICT"))
                .andExpect(jsonPath("$.statusMessage").value("Insufficient balance"));

        verify(voucherOrderService).order(voucherId, userId);
    }

    @Test
    void findAllByUserIdAndStatus_AsOwner_Returns200() throws Exception {
        UUID ownerId = UUID.randomUUID();

        when(voucherService.findAllByUserIdAndStatus(ownerId, VoucherStatus.PAID)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/vouchers/user/{userId}/status", ownerId)
                        .param("status", "PAID")
                        .with(user(customUser(ownerId, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"));

        verify(voucherService).findAllByUserIdAndStatus(ownerId, VoucherStatus.PAID);
    }

    @Test
    void findAllByUserIdAndStatus_AsAnotherUser_Returns403() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();

        mockMvc.perform(get("/api/vouchers/user/{userId}/status", ownerId)
                        .param("status", "PAID")
                        .with(user(customUser(strangerId, Role.USER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusMessage").value("Access denied"));

        verifyNoInteractions(voucherService);
    }

    @Test
    @WithMockUser
    void search_WithFilterAndPaging_PassesParametersToService() throws Exception {
        VoucherDTO dto = new VoucherDTO();
        dto.setTitle("Prague old town");
        PageResponseDTO<VoucherDTO> page = new PageResponseDTO<>(new PageImpl<>(List.of(dto), PageRequest.of(1, 5), 6));

        when(voucherService.search(any(VoucherFilterDTO.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/vouchers/search")
                        .param("tourType", "CULTURAL")
                        .param("minPrice", "100")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.results.page").value(1))
                .andExpect(jsonPath("$.results.size").value(5))
                .andExpect(jsonPath("$.results.totalElements").value(6))
                .andExpect(jsonPath("$.results.totalPages").value(2))
                .andExpect(jsonPath("$.results.content[0].title").value("Prague old town"));

        ArgumentCaptor<VoucherFilterDTO> filterCaptor = ArgumentCaptor.forClass(VoucherFilterDTO.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(voucherService).search(filterCaptor.capture(), pageableCaptor.capture());

        assertEquals(TourType.CULTURAL, filterCaptor.getValue().getTourType());
        assertEquals(0, new BigDecimal("100").compareTo(filterCaptor.getValue().getMinPrice()));
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
        assertEquals("price", pageableCaptor.getValue().getSort().iterator().next().getProperty());
    }

    @Test
    @WithMockUser
    void search_ArrivalFromAfterArrivalTo_Returns400() throws Exception {
        mockMvc.perform(get("/api/vouchers/search")
                        .param("arrivalFrom", "2027-08-31")
                        .param("arrivalTo", "2027-06-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.statusMessage").value(containsString("arrivalTo")));

        verifyNoInteractions(voucherService);
    }

    @Test
    @WithMockUser
    void search_UnsupportedSortField_Returns400() throws Exception {
        when(voucherService.search(any(VoucherFilterDTO.class), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("Unsupported sort field: foo"));

        mockMvc.perform(get("/api/vouchers/search").param("sort", "foo,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusMessage").value("Unsupported sort field: foo"));
    }

    @Test
    @WithMockUser
    void search_InvalidTourType_Returns400() throws Exception {
        mockMvc.perform(get("/api/vouchers/search").param("tourType", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("BAD_REQUEST"));

        verifyNoInteractions(voucherService);
    }

    @Test
    void search_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/vouchers/search"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(voucherService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createVoucher_ArrivalAfterEviction_Returns400() throws Exception {
        VoucherRequestDTO voucherDTO = new VoucherRequestDTO();
        voucherDTO.setTitle("Voucher title");
        voucherDTO.setDescription("Description");
        voucherDTO.setPrice(299.99);
        voucherDTO.setTourType(TourType.ADVENTURE);
        voucherDTO.setTransferType(TransferType.PLANE);
        voucherDTO.setHotelType(HotelType.FIVE_STARS);
        voucherDTO.setArrivalDate(LocalDate.of(2027, 9, 10));
        voucherDTO.setEvictionDate(LocalDate.of(2027, 9, 1));
        voucherDTO.setIsHot(false);

        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(voucherDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusMessage").value(containsString("evictionDate")));

        verifyNoInteractions(voucherService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createVoucher_ClientSendsIdStatusAndUser_IgnoresThem() throws Exception {
        String body = "{\"id\":\"" + UUID.randomUUID() + "\",\"title\":\"Title\",\"description\":\"Description\","
                + "\"price\":100.0,\"tourType\":\"ECO\",\"transferType\":\"BUS\",\"hotelType\":\"THREE_STARS\","
                + "\"status\":\"PAID\",\"userId\":\"" + UUID.randomUUID() + "\","
                + "\"arrivalDate\":\"2027-09-01\",\"evictionDate\":\"2027-09-05\",\"isHot\":false}";

        when(voucherService.create(any(VoucherRequestDTO.class))).thenReturn(new VoucherDTO());

        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(voucherService).create(any(VoucherRequestDTO.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createVoucher_UnknownTourType_Returns400() throws Exception {
        String body = "{\"title\":\"Title\",\"description\":\"Description\",\"price\":100.0,\"tourType\":\"FOO\","
                + "\"transferType\":\"BUS\",\"hotelType\":\"THREE_STARS\","
                + "\"arrivalDate\":\"2027-09-01\",\"evictionDate\":\"2027-09-05\",\"isHot\":false}";

        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("BAD_REQUEST"));

        verifyNoInteractions(voucherService);
    }

}
