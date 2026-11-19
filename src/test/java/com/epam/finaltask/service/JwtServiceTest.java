package com.epam.finaltask.service;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.token.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class JwtServiceTest {


    private static final String SECRET = "404E635263336A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String SECRET1 = "404E635263336A586E3272357538782F413F4428472B4B6250645367566B5971";


    @Test
    void extractUsername_ValidToken_ReturnsUsername() {

        String username = "John";
        User user = new User();

        user.setUsername(username);
        user.setRole(Role.ADMIN);

        JwtService jwtService = new JwtService(SECRET, 55555);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(customUserDetails);

        Assertions.assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void extractRole_ValidToken_ReturnsRoleWithPrefix() {

        String username = "John";
        User user = new User();
        user.setRole(Role.ADMIN);

        JwtService jwtService = new JwtService(SECRET, 55555);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(customUserDetails);

        Assertions.assertEquals("ROLE_ADMIN", jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_FreshToken_ReturnsTrue() {

        String username = "John";
        User user = new User();

        user.setUsername(username);
        user.setRole(Role.ADMIN);

        JwtService jwtService = new JwtService(SECRET, 55555);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(customUserDetails);

        Assertions.assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {

        String username = "John";
        User user = new User();

        user.setUsername(username);
        user.setRole(Role.ADMIN);

        JwtService jwtService = new JwtService(SECRET, -1000);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(customUserDetails);

        Assertions.assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_TokenSignedWithForeignKey_ReturnsFalse() {

        String username = "John";
        User user = new User();

        user.setUsername(username);
        user.setRole(Role.ADMIN);

        JwtService jwtService = new JwtService(SECRET, 1000);
        JwtService jwtService2 = new JwtService(SECRET1, 1000);

        CustomUserDetails customUserDetails1 = new CustomUserDetails(user);
        String token1 = jwtService2.generateToken(customUserDetails1);

        Assertions.assertFalse(jwtService.isTokenValid(token1));
    }

    @Test
    void isTokenValid_GarbageString_ReturnsFalse() {

        JwtService jwtService = new JwtService(SECRET, 1000);

        String token = "not valid";

        Assertions.assertFalse(jwtService.isTokenValid(token));
    }


}
