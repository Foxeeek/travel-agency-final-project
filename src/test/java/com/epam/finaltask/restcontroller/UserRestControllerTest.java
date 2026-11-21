package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class UserRestControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_AsAdmin_Returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO dto = new UserDTO();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("User is successfully blocked"));

        verify(userService).changeAccountStatus(any(UserDTO.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void changeUserStatus_AsManager_Returns403() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO dto = new UserDTO();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusMessage").value("Access denied"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_MissingActive_Returns400() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO dto = new UserDTO();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusMessage").value("active: is required"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeUserStatus_UserNotFound_Returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(patch("/api/users/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusMessage").value("User not found"));

        verify(userService).changeAccountStatus(any(UserDTO.class));
    }
}
