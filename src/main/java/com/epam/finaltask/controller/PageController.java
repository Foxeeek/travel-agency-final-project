package com.epam.finaltask.controller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.LoginRequestDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherOrderService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JwtAuthenticationFilter;
import com.epam.finaltask.token.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;


@Controller
public class PageController {
    VoucherService voucherService;
    UserService userService;
    JwtService jwtService;
    AuthenticationManager authenticationManager;
    VoucherOrderService voucherOrderService;

    public PageController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserService userService, VoucherService voucherService,
                          VoucherOrderService voucherOrderService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.voucherService = voucherService;
        this.voucherOrderService = voucherOrderService;
    }



    @GetMapping("/vouchers")
    public String vouchers(@ModelAttribute("filter") VoucherFilterDTO filter,
                           @PageableDefault(size = 10, sort = "price") Pageable pageable,
                           Model model) {
        model.addAttribute("page", voucherService.search(filter,pageable));
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

    @PostMapping("/vouchers/{id}/buy")
    @PreAuthorize("hasRole('USER')")
    public String order(@PathVariable UUID id,
                      @AuthenticationPrincipal CustomUserDetails principal,
                      RedirectAttributes redirect) {
        try {
            voucherOrderService.order(id, principal.getUser().getId());
            redirect.addFlashAttribute("message", "Ordered!");

        } catch (ConflictException | NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/vouchers";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails principal,
                          Model model) {
        UUID id = principal.getUser().getId();
        model.addAttribute("profile", userService.getProfile(id));
        model.addAttribute("vouchers", voucherService.findAllByUserId(id.toString()));
        return "profile";
    }


    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String manage(@PageableDefault(size = 20, sort = "title") Pageable pageable,
                         Model model) {

        model.addAttribute("page", voucherService.search(new VoucherFilterDTO(), pageable));
        return "manage";
    }

    @PostMapping("/manage/vouchers/{id}/hot")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String toggleHot(@PathVariable String id,
                                 @RequestParam boolean hot,
                                 RedirectAttributes redirect) {

        VoucherDTO dto = new VoucherDTO();
        dto.setIsHot(hot);
        voucherService.changeHotStatus(id, dto);

        redirect.addFlashAttribute("message", "Updated");
        return "redirect:/manage";
    }

    @PostMapping("/manage/orders/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String changeStatus(@PathVariable UUID id,
                               @RequestParam VoucherStatus status,           // нове значення статусу з форми
                               RedirectAttributes redirect) {
        try {
            voucherOrderService.changeStatus(id, status);
            redirect.addFlashAttribute("message", "Status changed");
        } catch (ConflictException | NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage";
    }
}