package com.epam.finaltask.controller;

import com.epam.finaltask.auth.CustomUserDetails;
import com.epam.finaltask.dto.PageResponseDTO;
import com.epam.finaltask.dto.UserResponseDTO;
import com.epam.finaltask.dto.UserUpdateRequestDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.NotFoundException;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherOrderService;
import com.epam.finaltask.service.VoucherService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private VoucherOrderService voucherOrderService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private static final UUID USER_ID = UUID.fromString("b1000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("f3e02ce0-365d-4c03-90a1-98f00cf6d3d2");

    private CustomUserDetails details(UUID id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("tester");
        user.setPassword("hash");
        user.setRole(role);
        user.setActive(true);
        return new CustomUserDetails(user);
    }

    private RequestPostProcessor asUser() {
        return user(details(USER_ID, Role.USER));
    }

    private RequestPostProcessor asManager() {
        return user(details(UUID.randomUUID(), Role.MANAGER));
    }

    private RequestPostProcessor asAdmin() {
        return user(details(ADMIN_ID, Role.ADMIN));
    }

    private VoucherDTO voucher(UUID userId) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setTitle("Rome weekend");
        dto.setDescription("Colosseum");
        dto.setPrice(480.0);
        dto.setTourType("CULTURAL");
        dto.setHotelType("FOUR_STARS");
        dto.setTransferType("PLANE");
        dto.setStatus("REGISTERED");
        dto.setArrivalDate(LocalDate.of(2027, 3, 5));
        dto.setEvictionDate(LocalDate.of(2027, 3, 8));
        dto.setIsHot(false);
        dto.setUserId(userId);
        return dto;
    }

    private PageResponseDTO<VoucherDTO> pageOf(VoucherDTO... items) {
        List<VoucherDTO> list = List.of(items);
        return new PageResponseDTO<>(new PageImpl<>(list, PageRequest.of(0, 10), list.size()));
    }

    private UserResponseDTO profileDto() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(USER_ID.toString());
        dto.setUsername("tester");
        dto.setRole("USER");
        dto.setPhoneNumber("+380501112233");
        dto.setBalance(100.0);
        dto.setActive(true);
        return dto;
    }

    @Nested
    class LoginAndLogoutTest {

        @Test
        void showLoginForm_Anonymous_ReturnsLoginPage() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/sign-in"))
                    .andExpect(model().attributeExists("loginRequest"));
        }

        @Test
        void login_ValidCredentials_SetsHttpOnlyStrictCookieAndRedirects() throws Exception {
            CustomUserDetails details = details(USER_ID, Role.USER);
            when(authenticationManager.authenticate(any()))
                    .thenReturn(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

            mockMvc.perform(post("/login").param("username", "tester").param("password", "Secret1!"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/vouchers"))
                    .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                            org.hamcrest.Matchers.startsWith("token="),
                            org.hamcrest.Matchers.containsString("HttpOnly"),
                            org.hamcrest.Matchers.containsString("SameSite=Strict"))));
        }

        @Test
        void login_BadCredentials_ReturnsLoginPageWithError() throws Exception {
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

            mockMvc.perform(post("/login").param("username", "tester").param("password", "wrong"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/sign-in"))
                    .andExpect(model().attribute("error", "Invalid username or password"))
                    .andExpect(header().doesNotExist("Set-Cookie"));
        }

        @Test
        void login_BadCredentialsWithUkrainianLocale_ShowsUkrainianError() throws Exception {
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

            mockMvc.perform(post("/login").cookie(new Cookie("lang", "uk"))
                            .param("username", "tester").param("password", "wrong"))
                    .andExpect(model().attribute("error", "Невірний логін або пароль"));
        }

        @Test
        void logout_Authenticated_ClearsCookieAndRedirectsToLogin() throws Exception {
            mockMvc.perform(post("/logout").with(asUser()))
                    .andExpect(redirectedUrl("/login"))
                    .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                            org.hamcrest.Matchers.containsString("Max-Age=0"),
                            org.hamcrest.Matchers.containsString("SameSite=Strict"))));
        }
    }

    @Nested
    class CatalogTest {

        @Test
        void vouchers_Anonymous_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/vouchers"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        void vouchers_AuthenticatedUser_ReturnsCatalogWithPage() throws Exception {
            when(voucherService.search(any(), any())).thenReturn(pageOf(voucher(null)));

            mockMvc.perform(get("/vouchers").with(asUser()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("vouchers"))
                    .andExpect(model().attributeExists("page", "filter"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Rome weekend")));
        }

        @Test
        void vouchers_WithFilterAndSort_PassesFilterAndPageableToService() throws Exception {
            when(voucherService.search(any(), any())).thenReturn(pageOf());

            mockMvc.perform(get("/vouchers").with(asUser())
                            .param("hotelType", "FIVE_STARS")
                            .param("minPrice", "100")
                            .param("page", "2")
                            .param("size", "5")
                            .param("sort", "price,desc"))
                    .andExpect(status().isOk());

            ArgumentCaptor<com.epam.finaltask.dto.VoucherFilterDTO> filter =
                    ArgumentCaptor.forClass(com.epam.finaltask.dto.VoucherFilterDTO.class);
            ArgumentCaptor<org.springframework.data.domain.Pageable> pageable =
                    ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
            verify(voucherService).search(filter.capture(), pageable.capture());

            assertEquals(com.epam.finaltask.model.HotelType.FIVE_STARS, filter.getValue().getHotelType());
            assertEquals(2, pageable.getValue().getPageNumber());
            assertEquals(5, pageable.getValue().getPageSize());
            assertEquals("price: DESC", pageable.getValue().getSort().toString());
        }

        @Test
        void vouchers_UserSeesBuyColumn_ManagerDoesNot() throws Exception {
            when(voucherService.search(any(), any())).thenReturn(pageOf(voucher(null)));

            mockMvc.perform(get("/vouchers").with(asUser()))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("/buy")));
            mockMvc.perform(get("/vouchers").with(asManager()))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("/buy"))));
        }

        @Test
        void vouchers_UkrainianLocale_RendersTranslatedTitleAndEnums() throws Exception {
            when(voucherService.search(any(), any())).thenReturn(pageOf(voucher(null)));

            mockMvc.perform(get("/vouchers").with(asUser()).cookie(new Cookie("lang", "uk")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Путівки")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("4 зірки")));
        }

        @Test
        void vouchers_LangParameter_SwitchesLocaleAndSetsCookie() throws Exception {
            when(voucherService.search(any(), any())).thenReturn(pageOf());

            mockMvc.perform(get("/vouchers").with(asUser()).param("lang", "uk"))
                    .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("lang=uk")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Путівки")));
        }
    }

    @Nested
    class BuyTest {

        @Test
        void buy_AsUser_OrdersForCurrentUserAndRedirectsWithMessage() throws Exception {
            UUID voucherId = UUID.randomUUID();

            mockMvc.perform(post("/vouchers/{id}/buy", voucherId).with(asUser()))
                    .andExpect(redirectedUrl("/vouchers"))
                    .andExpect(flash().attribute("message", "Ordered!"));

            verify(voucherOrderService).order(voucherId, USER_ID);
        }

        @Test
        void buy_UkrainianLocale_FlashMessageIsTranslated() throws Exception {
            mockMvc.perform(post("/vouchers/{id}/buy", UUID.randomUUID()).with(asUser())
                            .cookie(new Cookie("lang", "uk")))
                    .andExpect(flash().attribute("message", "Путівку замовлено!"));
        }

        @Test
        void buy_ServiceConflict_FlashesLocalizedError() throws Exception {
            UUID voucherId = UUID.randomUUID();
            doThrow(new ConflictException("Voucher " + voucherId + " is already ordered",
                    "err.voucher.alreadyOrdered", voucherId))
                    .when(voucherOrderService).order(voucherId, USER_ID);

            mockMvc.perform(post("/vouchers/{id}/buy", voucherId).with(asUser()))
                    .andExpect(redirectedUrl("/vouchers"))
                    .andExpect(flash().attribute("error", "Voucher " + voucherId + " is already ordered"));
        }

        @Test
        void buy_ConflictWithoutKey_FlashesPlainExceptionMessage() throws Exception {
            UUID voucherId = UUID.randomUUID();
            doThrow(new ConflictException("Plain text"))
                    .when(voucherOrderService).order(voucherId, USER_ID);

            mockMvc.perform(post("/vouchers/{id}/buy", voucherId).with(asUser()))
                    .andExpect(flash().attribute("error", "Plain text"));
        }

        @Test
        void buy_VoucherNotFound_FlashesUkrainianNotFoundMessage() throws Exception {
            UUID voucherId = UUID.randomUUID();
            doThrow(new NotFoundException("Voucher " + voucherId + " not found", "err.voucher.notFound", voucherId))
                    .when(voucherOrderService).order(voucherId, USER_ID);

            mockMvc.perform(post("/vouchers/{id}/buy", voucherId).with(asUser()).cookie(new Cookie("lang", "uk")))
                    .andExpect(flash().attribute("error", "Путівку " + voucherId + " не знайдено"));
        }

        @Test
        void buy_AsManager_IsForbidden() throws Exception {
            mockMvc.perform(post("/vouchers/{id}/buy", UUID.randomUUID()).with(asManager()))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error"));

            verifyNoInteractions(voucherOrderService);
        }
    }

    @Nested
    class ProfileTest {

        @Test
        void profile_Authenticated_ShowsProfileVouchersAndEmptyForm() throws Exception {
            when(userService.getProfile(USER_ID)).thenReturn(profileDto());
            when(voucherService.findAllByUserId(USER_ID.toString())).thenReturn(List.of(voucher(USER_ID)));

            mockMvc.perform(get("/profile").with(asUser()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile"))
                    .andExpect(model().attributeExists("profile", "vouchers", "profileForm"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Rome weekend")));
        }

        @Test
        void updateProfile_PhoneOnlyWithEmptyPassword_PassesNullPasswordAndRedirects() throws Exception {
            mockMvc.perform(post("/profile").with(asUser())
                            .param("phoneNumber", "+380991112233")
                            .param("password", ""))
                    .andExpect(redirectedUrl("/profile"))
                    .andExpect(flash().attribute("message", "Profile updated"));

            ArgumentCaptor<UserUpdateRequestDTO> captor = ArgumentCaptor.forClass(UserUpdateRequestDTO.class);
            verify(userService).updateProfile(eq(USER_ID), captor.capture());
            assertEquals("+380991112233", captor.getValue().getPhoneNumber());
            assertNull(captor.getValue().getPassword());
        }

        @Test
        void updateProfile_ValidPassword_PassesItToService() throws Exception {
            mockMvc.perform(post("/profile").with(asUser())
                            .param("phoneNumber", "")
                            .param("password", "Newpass1!"))
                    .andExpect(redirectedUrl("/profile"));

            ArgumentCaptor<UserUpdateRequestDTO> captor = ArgumentCaptor.forClass(UserUpdateRequestDTO.class);
            verify(userService).updateProfile(eq(USER_ID), captor.capture());
            assertNull(captor.getValue().getPhoneNumber());
            assertEquals("Newpass1!", captor.getValue().getPassword());
        }

        @Test
        void updateProfile_InvalidPhoneAndWeakPassword_ReturnsFormWithFieldErrors() throws Exception {
            when(userService.getProfile(USER_ID)).thenReturn(profileDto());

            mockMvc.perform(post("/profile").with(asUser())
                            .param("phoneNumber", "123")
                            .param("password", "weak"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile"))
                    .andExpect(model().attributeHasFieldErrors("profileForm", "phoneNumber", "password"))
                    .andExpect(model().attributeExists("profile", "vouchers"));

            verify(userService, never()).updateProfile(any(), any());
        }

        @Test
        void updateProfile_PhoneUsedByAnotherUser_ShowsErrorUnderPhoneFieldAndKeepsProfileData() throws Exception {
            when(userService.getProfile(USER_ID)).thenReturn(profileDto());
            when(userService.updateProfile(eq(USER_ID), any())).thenThrow(
                    new ConflictException("Phone number +380509998877 already exists",
                            "err.user.phoneExists", "+380509998877"));

            mockMvc.perform(post("/profile").with(asUser()).param("phoneNumber", "+380509998877"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile"))
                    .andExpect(model().attributeHasFieldErrors("profileForm", "phoneNumber"))
                    .andExpect(model().attributeExists("profile", "vouchers"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "Phone number +380509998877 is already registered")));
        }

        @Test
        void updateProfile_InvalidPhoneUkrainianLocale_ShowsTranslatedValidationMessage() throws Exception {
            when(userService.getProfile(USER_ID)).thenReturn(profileDto());

            mockMvc.perform(post("/profile").with(asUser()).cookie(new Cookie("lang", "uk"))
                            .param("phoneNumber", "123"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("у міжнародному форматі")));
        }
    }

    @Nested
    class RegistrationTest {

        private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registration(
                String username, String phone, String password) {
            return post("/register")
                    .param("username", username)
                    .param("phoneNumber", phone)
                    .param("password", password);
        }

        @Test
        void showRegisterForm_Anonymous_ReturnsFormWithoutLogin() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeExists("registerForm"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("type=\"password\"")));
        }

        @Test
        void register_EmptyForm_ReturnsFormWithFieldErrorsAndDoesNotCallService() throws Exception {
            mockMvc.perform(registration("", "", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeHasFieldErrors("registerForm", "username", "phoneNumber", "password"));

            verifyNoInteractions(userService);
        }

        @Test
        void register_WeakPasswordAndBadPhone_ReturnsFieldErrorsWithoutEchoingPassword() throws Exception {
            mockMvc.perform(registration("john_doe", "123", "weakpass"))
                    .andExpect(model().attributeHasFieldErrors("registerForm", "phoneNumber", "password"))
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString("weakpass"))));
        }

        @Test
        void register_ValidForm_CreatesUserWithRoleUserAndRedirectsToLogin() throws Exception {
            mockMvc.perform(registration("john_doe", "+380501234567", "Newpass1!"))
                    .andExpect(redirectedUrl("/login"))
                    .andExpect(flash().attribute("message", "Registration successful. You can log in now."));

            ArgumentCaptor<com.epam.finaltask.dto.UserDTO> captor =
                    ArgumentCaptor.forClass(com.epam.finaltask.dto.UserDTO.class);
            verify(userService).register(captor.capture());
            assertEquals("john_doe", captor.getValue().getUsername());
            assertEquals("+380501234567", captor.getValue().getPhoneNumber());
            assertEquals("USER", captor.getValue().getRole());
            assertEquals(0.0, captor.getValue().getBalance());
            assertTrue(captor.getValue().isActive());
        }

        @Test
        void register_UkrainianLocale_FlashMessageIsTranslated() throws Exception {
            mockMvc.perform(registration("john_doe", "+380501234567", "Newpass1!").cookie(new Cookie("lang", "uk")))
                    .andExpect(flash().attribute("message", "Реєстрація пройшла успішно. Тепер можна увійти."));
        }

        @Test
        void register_UsernameTaken_ShowsErrorUnderUsernameField() throws Exception {
            when(userService.register(any())).thenThrow(
                    new ConflictException("User john_doe already exists", "err.user.exists", "john_doe"));

            mockMvc.perform(registration("john_doe", "+380501234567", "Newpass1!"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeHasFieldErrors("registerForm", "username"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("User john_doe already exists")));
        }

        @Test
        void register_PhoneTaken_ShowsErrorUnderPhoneFieldWithPhoneInMessage() throws Exception {
            when(userService.register(any())).thenThrow(
                    new ConflictException("Phone number +380501234567 already exists",
                            "err.user.phoneExists", "+380501234567"));

            mockMvc.perform(registration("john_doe", "+380501234567", "Newpass1!"))
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeHasFieldErrors("registerForm", "phoneNumber"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "Phone number +380501234567 is already registered")));
        }

        @Test
        void register_PhoneTakenUkrainianLocale_MessageIsTranslatedWithPhone() throws Exception {
            when(userService.register(any())).thenThrow(
                    new ConflictException("Phone number +380501234567 already exists",
                            "err.user.phoneExists", "+380501234567"));

            mockMvc.perform(registration("john_doe", "+380501234567", "Newpass1!").cookie(new Cookie("lang", "uk")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "Номер телефону +380501234567 уже зареєстровано")));
        }

        @Test
        void register_Authenticated_StillReachable() throws Exception {
            mockMvc.perform(get("/register").with(asUser()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class ManageTest {

        @Test
        void manage_AsManager_ShowsVouchers() throws Exception {
            when(voucherService.search(any(), any())).thenReturn(pageOf(voucher(null)));

            mockMvc.perform(get("/manage").with(asManager()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage"))
                    .andExpect(model().attributeExists("page"));
        }

        @Test
        void manage_AsUser_IsForbiddenWithErrorPage() throws Exception {
            mockMvc.perform(get("/manage").with(asUser()))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attribute("message", "Access denied"));
        }

        @Test
        void manage_AccessDeniedUkrainianLocale_ShowsTranslatedMessage() throws Exception {
            mockMvc.perform(get("/manage").with(asUser()).cookie(new Cookie("lang", "uk")))
                    .andExpect(model().attribute("message", "Доступ заборонено"));
        }

        @Test
        void manage_Anonymous_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/manage"))
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        void toggleHot_AsManager_CallsServiceWithHotFlag() throws Exception {
            mockMvc.perform(post("/manage/vouchers/{id}/hot", "abc").with(asManager()).param("hot", "true"))
                    .andExpect(redirectedUrl("/manage"))
                    .andExpect(flash().attribute("message", "Updated"));

            ArgumentCaptor<VoucherDTO> captor = ArgumentCaptor.forClass(VoucherDTO.class);
            verify(voucherService).changeHotStatus(eq("abc"), captor.capture());
            assertTrue(captor.getValue().getIsHot());
        }

        @Test
        void changeStatus_AsManager_CallsOrderService() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(post("/manage/orders/{id}/status", id).with(asManager()).param("status", "PAID"))
                    .andExpect(redirectedUrl("/manage"))
                    .andExpect(flash().attribute("message", "Status changed"));

            verify(voucherOrderService).changeStatus(id, VoucherStatus.PAID);
        }

        @Test
        void changeStatus_ServiceConflict_FlashesError() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new ConflictException("Only REGISTERED voucher can change its status", "err.status.onlyRegistered"))
                    .when(voucherOrderService).changeStatus(id, VoucherStatus.CANCELED);

            mockMvc.perform(post("/manage/orders/{id}/status", id).with(asManager()).param("status", "CANCELED"))
                    .andExpect(flash().attribute("error", "Only REGISTERED voucher can change its status"));
        }

        @Test
        void changeStatus_UnknownStatusValue_Returns400() throws Exception {
            mockMvc.perform(post("/manage/orders/{id}/status", UUID.randomUUID()).with(asManager())
                            .param("status", "NOPE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(view().name("error"))
                    .andExpect(model().attribute("message", "Invalid request parameter"));

            verifyNoInteractions(voucherOrderService);
        }

        @Test
        void deleteVoucher_AsAdmin_DeletesAndRedirects() throws Exception {
            mockMvc.perform(post("/manage/vouchers/{id}/delete", "v1").with(asAdmin()))
                    .andExpect(redirectedUrl("/manage"))
                    .andExpect(flash().attribute("message", "Deleted"));

            verify(voucherService).delete("v1");
        }

        @Test
        void deleteVoucher_OrderedVoucher_FlashesConflictMessage() throws Exception {
            doThrow(new ConflictException("Voucher is ordered and cannot be deleted", "err.voucher.orderedCannotDelete"))
                    .when(voucherService).delete("v1");

            mockMvc.perform(post("/manage/vouchers/{id}/delete", "v1").with(asAdmin()))
                    .andExpect(flash().attribute("error", "Voucher is ordered and cannot be deleted"));
        }

        @Test
        void deleteVoucher_AsManager_IsForbidden() throws Exception {
            mockMvc.perform(post("/manage/vouchers/{id}/delete", "v1").with(asManager()))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(voucherService);
        }
    }

    @Nested
    class VoucherFormTest {

        private void validForm(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder) {
            formWithDates(builder, "2027-03-05", "2027-03-08");
        }

        private void formWithDates(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                                   String arrival, String eviction) {
            builder.param("title", "Rome")
                    .param("description", "Nice")
                    .param("price", "120.50")
                    .param("tourType", "CULTURAL")
                    .param("hotelType", "FOUR_STARS")
                    .param("transferType", "PLANE")
                    .param("arrivalDate", arrival)
                    .param("evictionDate", eviction)
                    .param("isHot", "false");
        }

        @Test
        void newForm_AsAdmin_ReturnsEmptyForm() throws Exception {
            mockMvc.perform(get("/manage/vouchers/new").with(asAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("voucher-form"))
                    .andExpect(model().attributeExists("voucherForm"))
                    .andExpect(model().attributeDoesNotExist("voucherId"));
        }

        @Test
        void newForm_AsManager_IsForbidden() throws Exception {
            mockMvc.perform(get("/manage/vouchers/new").with(asManager()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void create_ValidForm_CreatesAndRedirects() throws Exception {
            var request = post("/manage/vouchers").with(asAdmin());
            validForm(request);

            mockMvc.perform(request)
                    .andExpect(redirectedUrl("/manage"))
                    .andExpect(flash().attribute("message", "Created"));

            ArgumentCaptor<VoucherRequestDTO> captor = ArgumentCaptor.forClass(VoucherRequestDTO.class);
            verify(voucherService).create(captor.capture());
            assertEquals("Rome", captor.getValue().getTitle());
            assertEquals(120.50, captor.getValue().getPrice());
            assertEquals(LocalDate.of(2027, 3, 8), captor.getValue().getEvictionDate());
        }

        @Test
        void create_EmptyForm_ReturnsFormWithFieldErrorsAndDoesNotCreate() throws Exception {
            mockMvc.perform(post("/manage/vouchers").with(asAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("voucher-form"))
                    .andExpect(model().attributeHasFieldErrors("voucherForm",
                            "title", "description", "price", "tourType", "hotelType", "transferType",
                            "arrivalDate", "evictionDate"));

            verify(voucherService, never()).create(any());
        }

        @Test
        void create_EvictionBeforeArrival_ReturnsFormWithError() throws Exception {
            var request = post("/manage/vouchers").with(asAdmin());
            formWithDates(request, "2027-03-10", "2027-03-08");

            mockMvc.perform(request)
                    .andExpect(view().name("voucher-form"))
                    .andExpect(model().attributeHasErrors("voucherForm"));

            verify(voucherService, never()).create(any());
        }

        @Test
        void create_EmptyFormUkrainianLocale_ShowsTranslatedErrors() throws Exception {
            mockMvc.perform(post("/manage/vouchers").with(asAdmin()).cookie(new Cookie("lang", "uk")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("обов&#39;язкове поле")));
        }

        @Test
        void editForm_ExistingVoucher_PrefillsFormAndSetsVoucherId() throws Exception {
            UUID id = UUID.randomUUID();
            when(voucherService.findById(id.toString())).thenReturn(voucher(null));

            mockMvc.perform(get("/manage/vouchers/{id}/edit", id).with(asAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("voucher-form"))
                    .andExpect(model().attribute("voucherId", id))
                    .andExpect(model().attribute("voucherForm",
                            org.hamcrest.Matchers.hasProperty("title", org.hamcrest.Matchers.is("Rome weekend"))));
        }

        @Test
        void update_ValidForm_UpdatesAndRedirects() throws Exception {
            UUID id = UUID.randomUUID();
            var request = post("/manage/vouchers/{id}", id).with(asAdmin());
            validForm(request);

            mockMvc.perform(request)
                    .andExpect(redirectedUrl("/manage"))
                    .andExpect(flash().attribute("message", "Updated"));

            verify(voucherService).update(eq(id.toString()), any(VoucherRequestDTO.class));
        }

        @Test
        void update_InvalidForm_StaysInEditModeWithVoucherId() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(post("/manage/vouchers/{id}", id).with(asAdmin()))
                    .andExpect(view().name("voucher-form"))
                    .andExpect(model().attribute("voucherId", id));

            verify(voucherService, never()).update(any(), any());
        }

        @Test
        void update_OrderedVoucher_FlashesConflictError() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new ConflictException("Voucher is ordered and cannot be changed", "err.voucher.orderedCannotUpdate"))
                    .when(voucherService).update(eq(id.toString()), any());
            var request = post("/manage/vouchers/{id}", id).with(asAdmin());
            validForm(request);

            mockMvc.perform(request)
                    .andExpect(redirectedUrl("/manage"))
                    .andExpect(flash().attribute("error", "Voucher is ordered and cannot be changed"));
        }

        @Test
        void update_VoucherNotFound_FlashesError() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new NotFoundException("Voucher " + id + " not found", "err.voucher.notFound", id))
                    .when(voucherService).update(eq(id.toString()), any());
            var request = post("/manage/vouchers/{id}", id).with(asAdmin());
            validForm(request);

            mockMvc.perform(request)
                    .andExpect(flash().attribute("error", "Voucher " + id + " not found"));
        }
    }

    @Nested
    class UsersAdminTest {

        private final UUID otherId = UUID.fromString("b1000000-0000-0000-0000-000000000003");

        @Test
        void users_AsAdmin_ShowsUsers() throws Exception {
            when(userService.findAll()).thenReturn(List.of(profileDto()));

            mockMvc.perform(get("/manage/users").with(asAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("users"))
                    .andExpect(model().attributeExists("users"));
        }

        @Test
        void users_AsManager_IsForbidden() throws Exception {
            mockMvc.perform(get("/manage/users").with(asManager()))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(userService);
        }

        @Test
        void blockUser_AsAdmin_BlocksAndFlashesMessage() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/status", otherId).with(asAdmin()).param("active", "false"))
                    .andExpect(redirectedUrl("/manage/users"))
                    .andExpect(flash().attribute("message", "Blocked"));

            verify(userService).changeAccountStatus(otherId, false);
        }

        @Test
        void unblockUser_AsAdmin_UnblocksAndFlashesMessage() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/status", otherId).with(asAdmin()).param("active", "true"))
                    .andExpect(flash().attribute("message", "Unblocked"));

            verify(userService).changeAccountStatus(otherId, true);
        }

        @Test
        void blockUser_Self_IsRejected() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/status", ADMIN_ID).with(asAdmin()).param("active", "false"))
                    .andExpect(redirectedUrl("/manage/users"))
                    .andExpect(flash().attribute("error", "You cannot block yourself"));

            verifyNoInteractions(userService);
        }

        @Test
        void blockUser_UnknownUser_FlashesLocalizedError() throws Exception {
            doThrow(new NotFoundException("User " + otherId + " not found", "err.user.notFound", otherId))
                    .when(userService).changeAccountStatus(otherId, false);

            mockMvc.perform(post("/manage/users/{id}/status", otherId).with(asAdmin()).cookie(new Cookie("lang", "uk"))
                            .param("active", "false"))
                    .andExpect(flash().attribute("error", "Користувача " + otherId + " не знайдено"));
        }

        @Test
        void changeRole_AsAdmin_ChangesRole() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/role", otherId).with(asAdmin()).param("role", "MANAGER"))
                    .andExpect(redirectedUrl("/manage/users"))
                    .andExpect(flash().attribute("message", "Role changed"));

            verify(userService).changeAccountRole(otherId, Role.MANAGER);
        }

        @Test
        void changeRole_Self_IsRejected() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/role", ADMIN_ID).with(asAdmin()).param("role", "USER"))
                    .andExpect(flash().attribute("error", "You cannot change your own role"));

            verifyNoInteractions(userService);
        }

        @Test
        void changeRole_UnknownUser_FlashesError() throws Exception {
            doThrow(new NotFoundException("User " + otherId + " not found", "err.user.notFound", otherId))
                    .when(userService).changeAccountRole(otherId, Role.USER);

            mockMvc.perform(post("/manage/users/{id}/role", otherId).with(asAdmin()).param("role", "USER"))
                    .andExpect(flash().attribute("error", "User " + otherId + " not found"));
        }

        @Test
        void changeRole_InvalidRole_Returns400WithoutCallingService() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/role", otherId).with(asAdmin()).param("role", "HACKER"))
                    .andExpect(status().isBadRequest())
                    .andExpect(view().name("error"));

            verifyNoInteractions(userService);
        }

        @Test
        void changeRole_AsManager_IsForbidden() throws Exception {
            mockMvc.perform(post("/manage/users/{id}/role", otherId).with(asManager()).param("role", "ADMIN"))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class ErrorHandlingTest {

        @Test
        void unexpectedException_ReturnsGenericErrorPageWithoutDetails() throws Exception {
            when(userService.getProfile(USER_ID)).thenThrow(new IllegalStateException("secret internal detail"));

            mockMvc.perform(get("/profile").with(asUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(view().name("error"))
                    .andExpect(model().attribute("message", "Something went wrong"))
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString("secret internal detail"))));
        }

        @Test
        void illegalArgumentFromService_Returns400Page() throws Exception {
            when(voucherService.search(any(), any())).thenThrow(new IllegalArgumentException("Unsupported sort field: x"));

            mockMvc.perform(get("/vouchers").with(asUser()).param("sort", "x"))
                    .andExpect(status().isBadRequest())
                    .andExpect(view().name("error"))
                    .andExpect(model().attribute("message", "Unsupported sort field: x"));
        }
    }
}
