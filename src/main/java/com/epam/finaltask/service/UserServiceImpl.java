package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public UserDTO updateUser(String username, UserDTO userDTO) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow( () -> {
                            log.warn("User {} not found", username);
                            return new NotFoundException("User " + username + " not found");
                }
                );



        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setVouchers(userDTO.getVouchers());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setBalance(BigDecimal.valueOf(userDTO.getBalance()));
        user.setActive(userDTO.isActive());

        userRepository.save(user);

        log.info("User {} updated successfully", user.getUsername());

        return userMapper.toUserDTO(user);

    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow( () -> {
                            log.warn("User {} not found", username);
                            return new NotFoundException("User " + username + " not found");
                        }
                );


        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO changeAccountStatus(UserDTO userDTO) {
        User user = userRepository.findById(UUID.fromString(userDTO.getId()))
                .orElseThrow( () -> {
                            log.warn("User with id {} not found", userDTO.getId());
                            return new NotFoundException("User " + userDTO.getId() + " not found");
                        }
                );
        User fromDTO = userMapper.toUser(userDTO);
        user.setActive(fromDTO.isActive());
        userRepository.save(user);
        log.info("Account status of user {} changed, active={}", userDTO.getUsername(), userDTO.isActive());
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow( () -> {
                            log.warn("User with id {} not found", id);
                            return new NotFoundException("User " + id + " not found");
                        }
                );

        log.debug("Found user by id {}", id);
        return userMapper.toUserDTO(user);
    }

}
