package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.UserUpdateRequestDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
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

import java.util.List;
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


    @Nested
    class ChangeRoleTest {

        @Test
        void changeAccountRole_UserExists_SetsRoleSavesAndReturnsDto() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setRole(Role.USER);
            UserResponseDTO dto = new UserResponseDTO();

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toUserResponseDTO(user)).thenReturn(dto);

            UserResponseDTO result = userService.changeAccountRole(id, Role.MANAGER);

            assertSame(dto, result);
            assertEquals(Role.MANAGER, user.getRole());
            verify(userRepository).save(user);
        }

        @Test
        void changeAccountRole_UserNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.changeAccountRole(id, Role.ADMIN));

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class FindAllTest {

        @Test
        void findAll_UsersExist_ReturnsMappedDtos() {
            User first = new User();
            User second = new User();
            UserResponseDTO firstDto = new UserResponseDTO();
            UserResponseDTO secondDto = new UserResponseDTO();

            when(userRepository.findAll()).thenReturn(List.of(first, second));
            when(userMapper.toUserResponseDTO(first)).thenReturn(firstDto);
            when(userMapper.toUserResponseDTO(second)).thenReturn(secondDto);

            List<UserResponseDTO> result = userService.findAll();

            assertEquals(List.of(firstDto, secondDto), result);
        }

        @Test
        void findAll_NoUsers_ReturnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertTrue(userService.findAll().isEmpty());

            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    class ProfileTest {

        @Test
        void getProfile_UserExists_ReturnsResponseDto() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            UserResponseDTO dto = new UserResponseDTO();

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toUserResponseDTO(user)).thenReturn(dto);

            UserResponseDTO result = userService.getProfile(id);

            assertSame(dto, result);
            verify(userRepository).findById(id);
        }

        @Test
        void getProfile_UserNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.getProfile(id));

            verifyNoInteractions(userMapper);
        }

        @Test
        void updateProfile_PhoneAndPassword_UpdatesBothAndEncodesPassword() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setPhoneNumber("+380501112233");
            user.setPassword("oldHash");

            UserUpdateRequestDTO request = new UserUpdateRequestDTO();
            request.setPhoneNumber("+380509998877");
            request.setPassword("NewPass456!");

            UserResponseDTO dto = new UserResponseDTO();

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewPass456!")).thenReturn("newHash");
            when(userMapper.toUserResponseDTO(user)).thenReturn(dto);

            UserResponseDTO result = userService.updateProfile(id, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertSame(dto, result);
            assertEquals("+380509998877", captor.getValue().getPhoneNumber());
            assertEquals("newHash", captor.getValue().getPassword());
        }

        @Test
        void updateProfile_OnlyPhone_DoesNotTouchPassword() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setPhoneNumber("+380501112233");
            user.setPassword("oldHash");

            UserUpdateRequestDTO request = new UserUpdateRequestDTO();
            request.setPhoneNumber("+380509998877");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toUserResponseDTO(user)).thenReturn(new UserResponseDTO());

            userService.updateProfile(id, request);

            assertEquals("+380509998877", user.getPhoneNumber());
            assertEquals("oldHash", user.getPassword());
            verifyNoInteractions(passwordEncoder);
            verify(userRepository).save(user);
        }

        @Test
        void updateProfile_OnlyPassword_EncodesPasswordAndKeepsPhone() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setPhoneNumber("+380501112233");
            user.setPassword("oldHash");

            UserUpdateRequestDTO request = new UserUpdateRequestDTO();
            request.setPassword("NewPass456!");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewPass456!")).thenReturn("newHash");
            when(userMapper.toUserResponseDTO(user)).thenReturn(new UserResponseDTO());

            userService.updateProfile(id, request);

            assertEquals("+380501112233", user.getPhoneNumber());
            assertEquals("newHash", user.getPassword());
            verify(userRepository).save(user);
        }

        @Test
        void updateProfile_UserNotFound_ThrowsNotFoundException() {
            UUID id = UUID.randomUUID();

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.updateProfile(id, new UserUpdateRequestDTO()));

            verify(userRepository, never()).save(any());
        }
    }
}
