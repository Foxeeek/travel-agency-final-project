package com.epam.finaltask.controller;

import com.epam.finaltask.dto.LoginRequestDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JwtAuthenticationFilter;
import com.epam.finaltask.token.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
public class PageController {
    VoucherService voucherService;
    UserService userService;
    JwtService jwtService;
    AuthenticationManager authenticationManager;

    public PageController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, VoucherService voucherService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.voucherService = voucherService;
    }



    @GetMapping("/vouchers")
    public String vouchers(Model model) {
        model.addAttribute("vouchers", voucherService.findAllHotFirst());
        return "vouchers";
    }

    @GetMapping("/login")
    public String showForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "auth/sign-in";
    }

    @PostMapping("/login")
    public String submit(@ModelAttribute("loginRequest") LoginRequestDTO form,
                         HttpServletResponse response,
                         Model model) {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword()));

            UserDetails details = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(details);

            ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(86400)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return "redirect:/vouchers";

        } catch (AuthenticationException e) {
            model.addAttribute("error", "Invalid username or password");
            return "auth/sign-in";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return "redirect:/login";
    }

}
