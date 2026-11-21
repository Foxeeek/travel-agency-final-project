package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.UserUpdateRequestDTO;

import java.util.UUID;

public interface UserService {
    UserDTO register(UserDTO userDTO);

    UserDTO getUserByUsername(String username);

    UserResponseDTO changeAccountStatus(UUID id, boolean status);

    UserDTO getUserById(UUID id);

    UserResponseDTO getProfile(UUID id);

    UserResponseDTO updateProfile(UUID id, UserUpdateRequestDTO request);
}
