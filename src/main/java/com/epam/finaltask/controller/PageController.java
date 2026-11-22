package com.epam.finaltask.controller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.LoginRequestDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherOrderService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JwtAuthenticationFilter;
import com.epam.finaltask.token.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        model.addAttribute("page", voucherService.search(filter, pageable));
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
                               @RequestParam VoucherStatus status,
                               RedirectAttributes redirect) {
        try {
            voucherOrderService.changeStatus(id, status);
            redirect.addFlashAttribute("message", "Status changed");
        } catch (ConflictException | NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage";
    }

    @PostMapping("/manage/vouchers/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteVoucher(@PathVariable String id,
                                RedirectAttributes redirect) {
        try {
            voucherService.delete(id);
            redirect.addFlashAttribute("message", "Deleted");
        } catch (NotFoundException | ConflictException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage";
    }

    //    -------------------
    @GetMapping("/manage/vouchers/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newVoucherForm(Model model) {
        model.addAttribute("voucherForm", new VoucherRequestDTO());
        return "voucher-form";
    }

    @PostMapping("/manage/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public String createVoucher(@Valid @ModelAttribute("voucherForm") VoucherRequestDTO form,
                                BindingResult result,
                                RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "voucher-form";
        }

        voucherService.create(form);
        redirect.addFlashAttribute("message", "Created");
        return "redirect:/manage";
    }

    @GetMapping("/manage/vouchers/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editVoucherForm(@PathVariable UUID id, Model model) {

        VoucherDTO b = voucherService.findById(id.toString());

        VoucherRequestDTO form = new VoucherRequestDTO();
        form.setTitle(b.getTitle());
        form.setDescription(b.getDescription());
        form.setPrice(b.getPrice());
        form.setTourType(TourType.valueOf(b.getTourType()));
        form.setHotelType(HotelType.valueOf(b.getHotelType()));
        form.setTransferType(TransferType.valueOf(b.getTransferType()));
        form.setArrivalDate(b.getArrivalDate());
        form.setEvictionDate(b.getEvictionDate());
        form.setIsHot(b.getIsHot());

        model.addAttribute("voucherForm", form);
        model.addAttribute("voucherId", id);
        return "voucher-form";
    }

    @PostMapping("/manage/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateVoucher(@PathVariable UUID id,
                                @Valid @ModelAttribute("voucherForm") VoucherRequestDTO form,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("voucherId", id);
            return "voucher-form";
        }

        try {
            voucherService.update(id.toString(), form);
            redirect.addFlashAttribute("message", "Updated");
        } catch (NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage";
    }


    @GetMapping("/manage/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @PostMapping("/manage/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUsersStatus(@PathVariable UUID id,
                                    @RequestParam boolean active,
                                    @AuthenticationPrincipal CustomUserDetails principal,
                                    RedirectAttributes redirect) {

        if (id.equals(principal.getUser().getId())) {
            redirect.addFlashAttribute("error", "You cannot block yourself");
            return "redirect:/manage/users";
        }

        try {
            userService.changeAccountStatus(id, active);
            redirect.addFlashAttribute("message", active ? "Unblocked" : "Blocked");
        } catch (NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage/users";
    }
}