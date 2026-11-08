package com.epam.finaltask;


import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;



@DataJpaTest
public class RepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private EntityManager entityManager;


    private User buildUser(String username, String phoneNumber) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setPhoneNumber(phoneNumber);
        user.setBalance(new BigDecimal("500.00"));
        user.setActive(true);
        return user;
    }

    private Voucher buildVoucher(String title) {
        Voucher voucher = new Voucher();
        voucher.setTitle(title);
        voucher.setDescription("7 days in a 5-star hotel");
        voucher.setPrice(new BigDecimal("850.00"));
        voucher.setTourType(TourType.ADVENTURE);
        voucher.setTransferType(TransferType.PLANE);
        voucher.setHotelType(HotelType.FIVE_STARS);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setArrivalDate(LocalDate.of(2027, 6, 10));
        voucher.setEvictionDate(LocalDate.of(2027, 6, 17));
        voucher.setIsHot(true);
        return voucher;
    }


    @Test
    void createVoucher_ValidData_SuccessfullyCreated_Test() {
        User user = userRepository.save(buildUser("tester","+111111"));
        Voucher voucher = voucherRepository.save(buildVoucher("Summer in Antalya"));
        voucher.setUser(user);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertEquals("Summer in Antalya", voucherRepository.findById(voucher.getId()).get().getTitle());

        Assertions.assertEquals(voucher.getPrice(), voucherRepository.findById(voucher.getId()).get().getPrice());

        Assertions.assertEquals(user.getId(), voucherRepository.findById(voucher.getId()).get().getUser().getId());

    }

    @Test
    void shouldThrow_Exception_WhenUsername_AlreadyExists() {
        User user = userRepository.save(buildUser("tester","+222222"));
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            User user1 = userRepository.save(buildUser("tester","+333333"));
            userRepository.saveAndFlush(user1);
        });

    }

    @Test
    void shouldAllowVoucher_WithoutAssignedUser() {
        Voucher voucher = voucherRepository.save(buildVoucher("Summer in Antalya"));
        voucher.setUser(null);
        voucherRepository.save(voucher);
        entityManager.flush();
        entityManager.clear();
        Assertions.assertNull(voucherRepository.findById(voucher.getId()).get().getUser());

    }


    @Test
    void should_FindVouchers_ByUserId() {

        User john = userRepository.save(buildUser("john","+444444"));
        User paul = userRepository.save(buildUser("paul","+555555"));


        Voucher antalya = voucherRepository.save(buildVoucher("Summer in Antalya"));
        Voucher paris = voucherRepository.save(buildVoucher("Paris City Tour"));
        Voucher milan = voucherRepository.save(buildVoucher("Shopping in Milan"));

        paris.setIsHot(false);
        milan.setIsHot(false);

        antalya.setUser(john);
        paris.setUser(john);
        milan.setUser(paul);


        entityManager.flush();
        entityManager.clear();

        Assertions.assertEquals(2, voucherRepository.findAllByUserId(john.getId()).size());
        Assertions.assertEquals(1, voucherRepository.findAllByUserId(paul.getId()).size());
    }

}
