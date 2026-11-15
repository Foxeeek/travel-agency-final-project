package com.epam.finaltask.service;


import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;


    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        String username = "existingUser";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        Assertions.assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(username));

        verify(userRepository).findUserByUsername(username);

    }


    @Test
    void loadUserByUsername_UserExists_ReturnsDetailsWithRoleAndEnabledFlag() {
        String username = "existingUser";
        User user = new User();
        user.setUsername(username);
        user.setRole(Role.ADMIN);
        user.setActive(true);

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername(username);
        Assertions.assertEquals(username, result.getUsername());
        Assertions.assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
        Assertions.assertEquals(true, result.isEnabled());

        verify(userRepository).findUserByUsername(username);
    }

}
