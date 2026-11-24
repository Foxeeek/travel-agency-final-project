package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.LoginRequestDTO;
import com.epam.finaltask.dto.RegisterRequestDTO;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.token.JwtAuthenticationFilter;
import com.epam.finaltask.token.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationRestController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationRestController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequestDTO register) {
        UserDTO userDTO = new UserDTO();

        userDTO.setUsername(register.getUsername());
        userDTO.setPassword(register.getPassword());
        userDTO.setRole(Role.USER.toString());
        userDTO.setPhoneNumber(register.getPhoneNumber());
        userDTO.setBalance(0.0);
        userDTO.setActive(true);

        UserDTO fromBD = userService.register(userDTO);
        fromBD.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("CREATED", "User was successfully created", fromBD));
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequestDTO login) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String authToken = jwtService.generateToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, authToken)
                .httpOnly(true).path("/")
                .maxAge(86400)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new ApiResponse<>("OK", "Login successful", authToken));
    }

    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, "")
                .httpOnly(true).path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new ApiResponse<>("OK", "Logout successful", null));
    }


}
