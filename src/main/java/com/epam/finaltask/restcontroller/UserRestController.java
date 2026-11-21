package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserStatusRequestDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> changeUserStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusRequestDTO request){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id.toString());
        userDTO.setActive(request.getActive());
        UserDTO result = userService.changeAccountStatus(userDTO);
        result.setPassword(null);

        return ResponseEntity.ok(new ApiResponse<>("OK", request.getActive() ? "User is successfully unblocked" : "User is successfully blocked", result));
    }

}
