package com.epam.finaltask.service;

import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.UserUpdateRequestDTO;

public interface UserService {
    UserDTO register(UserDTO userDTO);

    UserDTO getUserByUsername(String username);
    UserDTO changeAccountStatus(UserDTO userDTO);
    UserDTO getUserById(UUID id);
    UserResponseDTO getProfile(UUID id);
    UserResponseDTO updateProfile(UUID id, UserUpdateRequestDTO request);
}
