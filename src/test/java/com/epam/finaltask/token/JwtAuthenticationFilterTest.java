package com.epam.finaltask.token;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CustomUserDetails details(boolean active) {
        User user = new User();
        user.setUsername("john");
        user.setRole(Role.USER);
        user.setActive(active);
        return new CustomUserDetails(user);
    }

    @Test
    void doFilter_NoToken_ContinuesChainWithoutAuthentication() throws Exception {
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilter_InvalidToken_ContinuesChainWithoutAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer abc");
        when(jwtService.isTokenValid("abc")).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilter_ValidTokenInCookie_AuthenticatesUser() throws Exception {
        request.setCookies(new Cookie(JwtAuthenticationFilter.COOKIE_NAME, "valid.jwt"));
        when(jwtService.isTokenValid("valid.jwt")).thenReturn(true);
        when(jwtService.extractUsername("valid.jwt")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(details(true));

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("john", authentication.getName());
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_ValidBearerToken_AuthenticatesUser() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt");
        when(jwtService.isTokenValid("valid.jwt")).thenReturn(true);
        when(jwtService.extractUsername("valid.jwt")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(details(true));

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("john", authentication.getName());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_BothBearerHeaderAndCookie_UsesTheBearerHeader() throws Exception {
        request.addHeader("Authorization", "Bearer header.jwt");
        request.setCookies(new Cookie(JwtAuthenticationFilter.COOKIE_NAME, "cookie.jwt"));
        when(jwtService.isTokenValid("header.jwt")).thenReturn(true);
        when(jwtService.extractUsername("header.jwt")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(details(true));

        filter.doFilter(request, response, chain);

        assertEquals("john", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(jwtService).isTokenValid("header.jwt");
        verify(jwtService, never()).isTokenValid("cookie.jwt");
    }

    @Test
    void doFilter_NonBearerAuthorizationHeaderAndCookie_FallsBackToCookie() throws Exception {
        request.addHeader("Authorization", "Basic abc");
        request.setCookies(new Cookie(JwtAuthenticationFilter.COOKIE_NAME, "cookie.jwt"));
        when(jwtService.isTokenValid("cookie.jwt")).thenReturn(true);
        when(jwtService.extractUsername("cookie.jwt")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(details(true));

        filter.doFilter(request, response, chain);

        assertEquals("john", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilter_BlockedUser_DoesNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt");
        when(jwtService.isTokenValid("valid.jwt")).thenReturn(true);
        when(jwtService.extractUsername("valid.jwt")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(details(false));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}
