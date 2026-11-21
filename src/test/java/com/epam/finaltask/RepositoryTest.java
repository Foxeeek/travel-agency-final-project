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
import java.util.List;


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
        User user = userRepository.save(buildUser("tester", "+111111"));
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
        User user = userRepository.save(buildUser("tester", "+222222"));
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            User user1 = userRepository.save(buildUser("tester", "+333333"));
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

        User john = userRepository.save(buildUser("john", "+444444"));
        User paul = userRepository.save(buildUser("paul", "+555555"));


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

    @Test
    void should_FindAll_HotFirst() {
        Voucher antalya = voucherRepository.save(buildVoucher("Summer in Antalya"));
        Voucher paris = voucherRepository.save(buildVoucher("Paris City Tour"));
        Voucher milan = voucherRepository.save(buildVoucher("Shopping in Milan"));

        antalya.setIsHot(false);
        antalya.setPrice(new BigDecimal(500));


        paris.setPrice(new BigDecimal(900));

        milan.setIsHot(false);
        milan.setPrice(new BigDecimal(499));

        entityManager.flush();
        entityManager.clear();

        List<Voucher> voucherList = voucherRepository.findAllHotFirst();

        Assertions.assertEquals("Paris City Tour", voucherList.get(0).getTitle());
        Assertions.assertEquals("Shopping in Milan", voucherList.get(1).getTitle());
        Assertions.assertEquals("Summer in Antalya", voucherList.get(2).getTitle());

    }

    @Test
    void should_findAll_ByPriceRange() {
        Voucher antalya = voucherRepository.save(buildVoucher("Summer in Antalya"));
        Voucher paris = voucherRepository.save(buildVoucher("Paris City Tour"));
        Voucher milan = voucherRepository.save(buildVoucher("Shopping in Milan"));

        antalya.setPrice(new BigDecimal(500));
        paris.setPrice(new BigDecimal(900));
        milan.setPrice(new BigDecimal(1499));

        entityManager.flush();
        entityManager.clear();

        List<Voucher> voucherList = voucherRepository.findAllByPriceRange(new BigDecimal(400), new BigDecimal(900));

        Assertions.assertEquals(2, voucherList.size());
        Assertions.assertEquals("Summer in Antalya", voucherList.get(0).getTitle());
        Assertions.assertEquals("Paris City Tour", voucherList.get(1).getTitle());
    }

    @Test
    void should_FindAll_UserIdAndStatus() {
        User john = userRepository.save(buildUser("john", "+444444"));
        User paul = userRepository.save(buildUser("paul", "+555555"));

        Voucher antalya = voucherRepository.save(buildVoucher("Summer in Antalya"));
        Voucher paris = voucherRepository.save(buildVoucher("Paris City Tour"));
        Voucher milan = voucherRepository.save(buildVoucher("Shopping in Milan"));

        paris.setUser(john);
        paris.setStatus(VoucherStatus.PAID);

        antalya.setUser(john);

        milan.setUser(paul);
        milan.setStatus(VoucherStatus.PAID);

        entityManager.flush();
        entityManager.clear();

        List<Voucher> voucherList = voucherRepository.findAllByUserIdAndStatus(john.getId(), VoucherStatus.PAID);

        Assertions.assertEquals(1, voucherList.size());
    }


    @Test
    void should_FindAll_ByArrivalPeriod() {
        Voucher antalya = voucherRepository.save(buildVoucher("Summer in Antalya"));
        Voucher paris = voucherRepository.save(buildVoucher("Paris City Tour"));
        Voucher milan = voucherRepository.save(buildVoucher("Shopping in Milan"));


        antalya.setArrivalDate(LocalDate.of(2027, 7, 10));
        paris.setArrivalDate(LocalDate.of(2027, 6, 12));
        milan.setArrivalDate(LocalDate.of(2027, 6, 16));

        entityManager.flush();
        entityManager.clear();

        List<Voucher> voucherList = voucherRepository.findAllByArrivalPeriod(LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 29));

        Assertions.assertEquals(2, voucherList.size());
        Assertions.assertEquals("Paris City Tour", voucherList.get(0).getTitle());
        Assertions.assertEquals("Shopping in Milan", voucherList.get(1).getTitle());
    }
}
