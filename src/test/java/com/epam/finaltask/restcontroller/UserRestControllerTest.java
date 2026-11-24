package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.UserUpdateRequestDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class UserRestControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private VoucherService voucherService;

    @Autowired
    private MockMvc mockMvc;

    private CustomUserDetails customUser(UUID id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("john_doe");
        user.setRole(role);
        user.setActive(true);
        return new CustomUserDetails(user);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_AsAdmin_Returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO();

        when(userService.changeAccountStatus(eq(id), eq(false))).thenReturn(dto);

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("User is successfully blocked"));

        verify(userService).changeAccountStatus(eq(id), eq(false));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_Unblock_ReturnsUnblockedMessage() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO();
        dto.setActive(true);

        when(userService.changeAccountStatus(eq(id), eq(true))).thenReturn(dto);

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("User is successfully unblocked"))
                .andExpect(jsonPath("$.results.active").value(true));
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void changeUserStatus_AsManager_Returns403() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusMessage").value("Access denied"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_MissingActive_Returns400() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusMessage").value("active: is required"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_UserNotFound_Returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.changeAccountStatus(eq(id), eq(false))).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusMessage").value("User not found"));

        verify(userService).changeAccountStatus(eq(id), eq(false));
    }

    @Test
    void getProfile_AuthenticatedUser_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername("john_doe");

        when(userService.getProfile(userId)).thenReturn(dto);

        mockMvc.perform(get("/api/users/me").with(user(customUser(userId, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("Profile is successfully found"))
                .andExpect(jsonPath("$.results.username").value("john_doe"));

        verify(userService).getProfile(userId);
    }

    @Test
    void getProfile_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("UNAUTHORIZED"));

        verifyNoInteractions(userService);
    }


    @Test
    void updateProfile_ValidData_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO();
        dto.setPhoneNumber("+380509998877");

        when(userService.updateProfile(eq(userId), any(UserUpdateRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/users/me")
                        .with(user(customUser(userId, Role.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+380509998877\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("Profile is successfully updated"))
                .andExpect(jsonPath("$.results.phoneNumber").value("+380509998877"));

        ArgumentCaptor<UserUpdateRequestDTO> captor = ArgumentCaptor.forClass(UserUpdateRequestDTO.class);
        verify(userService).updateProfile(eq(userId), captor.capture());
        assertEquals("+380509998877", captor.getValue().getPhoneNumber());
        assertNull(captor.getValue().getPassword());
    }

    @Test
    void updateProfile_WeakPassword_Returns400() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/me")
                        .with(user(customUser(userId, Role.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("BAD_REQUEST"));

        verifyNoInteractions(userService);
    }

    @Test
    void updateProfile_UnsupportedContentType_Returns415() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/me")
                        .with(user(customUser(userId, Role.USER)))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("phone"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.statusCode").value("UNSUPPORTED_MEDIA_TYPE"));

        verifyNoInteractions(userService);
    }

    @Test
    void getMyVouchers_ReturnsVouchersOfCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        VoucherDTO voucher = new VoucherDTO();
        voucher.setTitle("Rome weekend");

        when(voucherService.findAllByUserId(userId.toString())).thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/users/me/vouchers").with(user(customUser(userId, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("Vouchers are successfully found"))
                .andExpect(jsonPath("$.results[0].title").value("Rome weekend"));

        verify(voucherService).findAllByUserId(userId.toString());
    }
}
