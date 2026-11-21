package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    class BaseTest {

        @Test
        void getUserByUsername_UserExists_Success() {
            // Given
            String username = "existingUser";
            User user = new User();
            user.setUsername(username);

            UserDTO expectedUserDTO = new UserDTO();
            expectedUserDTO.setUsername(username);

            when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
            when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);

            // When
            UserDTO result = userService.getUserByUsername(username);

            // Then
            assertNotNull(result, "The returned UserDTO should not be null");
            assertEquals(expectedUserDTO.getUsername(), result.getUsername(),
                    "The username should match the expected value");

            verify(userRepository, times(1)).findUserByUsername(username);
            verify(userMapper, times(1)).toUserDTO(any(User.class));
        }

        @Test
        void changeAccountStatus_UserExist_Success() {
            // Given
            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);
            user.setActive(false);

            UserResponseDTO responseDTO = new UserResponseDTO();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toUserResponseDTO(user)).thenReturn(responseDTO);

            // When
            UserResponseDTO result = userService.changeAccountStatus(userId, true);

            // Then
            assertSame(responseDTO, result);
            assertTrue(user.isActive(), "The account status should be updated to true");

            verify(userRepository).save(user);
        }


        @Test
        void getUserById_UserExist_Success() {
            // Given
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);

            UserDTO expectedUserDTO = new UserDTO();
            expectedUserDTO.setId(id.toString());

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);

            // When
            UserDTO resultDTO = userService.getUserById(id);

            // Then
            assertNotNull(resultDTO, "The returned UserDTO should not be null");
            assertEquals(expectedUserDTO.getId(), resultDTO.getId(),
                    "The user ID should match the expected value");

            verify(userRepository, times(1)).findById(id);
            verify(userMapper, times(1)).toUserDTO(any(User.class));
        }
    }

    @Nested
    class RegisterTest {
        @Test
        void register_NewUsername_EncodesPasswordAndSavesUser() {
            String usrName = "Jonathan";
            String rawPwd = "12345678";

            User user = new User();
            user.setUsername(usrName);
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(usrName);
            userDTO.setPassword(rawPwd);


            when(userRepository.existsByUsername(usrName)).thenReturn(false);
            when(passwordEncoder.encode(rawPwd)).thenReturn("hashedPassword");
            when(userMapper.toUserDTO(user)).thenReturn(userDTO);
            when(userMapper.toUser(userDTO)).thenReturn(user);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            UserDTO result = userService.register(userDTO);

            verify(userRepository).save(captor.capture());

            assertSame(userDTO, result);
            assertSame(user, captor.getValue());

            assertEquals("hashedPassword", userDTO.getPassword());

        }

        @Test
        void register_UsernameAlreadyTaken_ThrowsConflictException() {
            String usrName = "Jonathan";

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(usrName);

            when(userRepository.existsByUsername(usrName)).thenReturn(true);

            assertThrows(ConflictException.class, () -> userService.register(userDTO));

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class UserLookupTest {

        @Test
        void getUserByUsername_UserNotFound_ThrowsNotFoundException() {
            String username = "existingUser";

            when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

            Assertions.assertThrows(NotFoundException.class, () -> userService.getUserByUsername(username));

            verify(userRepository).findUserByUsername(username);

        }

        @Test
        void getUserByUsername_UserExists_ReturnsDto() {
            String username = "existingUser";
            User user = new User();
            user.setUsername(username);
            UserDTO expectedUserDTO = new UserDTO();
            expectedUserDTO.setUsername(username);

            when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
            when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);

            Assertions.assertEquals(expectedUserDTO, userService.getUserByUsername(username));

            verify(userRepository, times(1)).findUserByUsername(username);
            verify(userMapper, times(1)).toUserDTO(any(User.class));

        }

        @Test
        void getUserById_UserNotFound_ThrowsNotFoundException() {
            UUID uuid = UUID.randomUUID();

            when(userRepository.findById(uuid)).thenReturn(Optional.empty());

            Assertions.assertThrows(NotFoundException.class, () -> userService.getUserById(uuid));

            verify(userRepository).findById(uuid);

        }

        @Test
        void getUserById_UserExists_ReturnsDto() {
            UUID uuid = UUID.randomUUID();
            User user = new User();
            user.setId(uuid);

            UserDTO expectedUserDTO = new UserDTO();
            expectedUserDTO.setId(uuid.toString());

            when(userRepository.findById(uuid)).thenReturn(Optional.of(user));
            when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);

            Assertions.assertEquals(expectedUserDTO, userService.getUserById(uuid));

            verify(userRepository, times(1)).findById(uuid);
            verify(userMapper, times(1)).toUserDTO(any(User.class));

        }


        @Test
        void changeAccountStatus_UserNotFound_ThrowsNotFoundException() {
            UUID uuid = UUID.randomUUID();

            when(userRepository.findById(uuid)).thenReturn(Optional.empty());

            Assertions.assertThrows(NotFoundException.class, () -> userService.changeAccountStatus(uuid, true));

            verify(userRepository, never()).save(any());
        }


    }
}
