package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class AuthenticationRestControllerTest {

    private static final String VALID_REGISTER =
            "{\"username\":\"johnny\",\"password\":\"Secret123!\",\"phoneNumber\":\"+380501112233\"}";

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_ValidData_Returns201WithoutPassword() throws Exception {
        UserDTO saved = new UserDTO();
        saved.setUsername("johnny");
        saved.setPassword("hashed");
        saved.setRole("USER");

        when(userService.register(any(UserDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("CREATED"))
                .andExpect(jsonPath("$.results.username").value("johnny"))
                .andExpect(jsonPath("$.results.password").isEmpty());

        verify(userService).register(any(UserDTO.class));
    }

    @Test
    void register_ClientSendsRoleAndBalance_IgnoresThem() throws Exception {
        UserDTO saved = new UserDTO();
        saved.setUsername("johnny");

        when(userService.register(any(UserDTO.class))).thenReturn(saved);

        String body = "{\"username\":\"johnny\",\"password\":\"Secret123!\",\"phoneNumber\":\"+380501112233\","
                + "\"role\":\"ADMIN\",\"balance\":999999,\"active\":false}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<UserDTO> captor = ArgumentCaptor.forClass(UserDTO.class);
        verify(userService).register(captor.capture());

        assertEquals("USER", captor.getValue().getRole());
        assertEquals(0.0, captor.getValue().getBalance());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void register_WeakPassword_Returns400() throws Exception {
        String body = "{\"username\":\"johnny\",\"password\":\"123\",\"phoneNumber\":\"+380501112233\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.statusMessage").value(containsString("password")));

        verifyNoInteractions(userService);
    }

    @Test
    void register_UsernameTaken_Returns409() throws Exception {
        when(userService.register(any(UserDTO.class))).thenThrow(new ConflictException("User johnny already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value("CONFLICT"))
                .andExpect(jsonPath("$.statusMessage").value("User johnny already exists"));
    }

    @Test
    void login_ValidCredentials_Returns200WithHttpOnlyCookie() throws Exception {
        User user = new User();
        user.setUsername("johnny");
        user.setRole(Role.USER);
        user.setActive(true);
        CustomUserDetails details = new CustomUserDetails(user);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"johnny\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.results").isNotEmpty());
    }

    @Test
    void login_WrongPassword_Returns401() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"johnny\",\"password\":\"Wrong123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.statusMessage").value("Invalid username or password"));
    }

    @Test
    void login_MissingFields_Returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void logout_ClearsTokenCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(jsonPath("$.statusMessage").value("Logout successful"));
    }
}
