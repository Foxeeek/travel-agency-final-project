package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.UserUpdateRequestDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserMapper userMapper, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    private User userById(UUID uuid){
        return userRepository.findById(uuid)
                .orElseThrow( () -> {
                            log.warn("User with id {} not found", uuid);
                            return new NotFoundException("User " + uuid + " not found");
                        }
                );
    }

    private User userByUsername(String username){
        return userRepository.findUserByUsername(username)
                .orElseThrow( () -> {
                            log.warn("User {} not found", username);
                            return new NotFoundException("User " + username + " not found");
                        }
                );
    }

    @Override
    public UserDTO register(UserDTO userDTO) {
        if (!userRepository.existsByUsername(userDTO.getUsername())) {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            User user = userMapper.toUser(userDTO);
            userRepository.save(user);
            log.info("User {} registered successfully", user.getUsername());
            return userMapper.toUserDTO(user);
        } else {
            log.warn("Registration rejected: user {} already exists", userDTO.getUsername());
            throw new ConflictException("User " + userDTO.getUsername() + " already exists");
        }


    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userByUsername(username);
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO changeAccountStatus(UserDTO userDTO) {
        User user = userById(UUID.fromString(userDTO.getId()));
        User fromDTO = userMapper.toUser(userDTO);
        user.setActive(fromDTO.isActive());
        userRepository.save(user);
        log.info("Account status of user {} changed, active={}", userDTO.getUsername(), userDTO.isActive());
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userById(id);
        log.debug("Found user by id {}", id);
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserResponseDTO getProfile(UUID id) {
        User user = userById(id);
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateProfile(UUID id, UserUpdateRequestDTO request) {
        User user = userById(id);
        if (request.getPhoneNumber() != null){
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPassword() != null){
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        log.info("User {} profile updated", user.getUsername());
        return userMapper.toUserResponseDTO(user);

    }

}
