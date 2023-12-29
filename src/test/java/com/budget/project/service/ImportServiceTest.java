package com.budget.project.service;

import static com.budget.project.utils.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.budget.project.auth.service.AuthService;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ImportServiceTest {
    @Autowired
    private ImportService importService;
    @Autowired
    private AuthService authService;
    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @BeforeEach
    void setUp() {
        registerUsers(authService, authenticationManager);
    }

    @Test
    @Transactional
    void importCSV() throws IOException {
        login(USER_1, authService);
        InputStream is = getClass().getClassLoader().getResourceAsStream("import/test.csv");
        MultipartFile file = new MockMultipartFile("test.csv", is);
        importService.importCSV(file);

        assertAll(
                () -> assertThat(accountService.getAccountByName("Santander").get().getTransactions().size()).isEqualTo(4),
                () -> assertThat(accountService.getAccountByName("mBank").get().getTransactions().size()).isEqualTo(3),
                () -> assertThat(accountService.getAccountByName("alior").get().getTransactions().size()).isEqualTo(1)
        );
    }

    @Test
    @Transactional
    void importOneMoneyCSV() throws IOException {
        login(USER_1, authService);
        InputStream is = getClass().getClassLoader().getResourceAsStream("import/onemoney.csv");
        MultipartFile file = new MockMultipartFile("onemoney.csv", is);
        importService.importFromOneMoney(file);

        assertAll(
                () -> assertThat(accountService.getAccountByName("Jakdojade").get().getTransactions().size()).isEqualTo(1),
                () -> assertThat(accountService.getAccountByName("Santander").get().getTransactions().size()).isEqualTo(3),
                () -> assertThat(accountService.getAccountByName("Koleo").get().getTransactions().size()).isEqualTo(2),
                () -> assertThat(accountService.getAccountByName("Rachunki").get().getTransactions().size()).isEqualTo(1),
                () -> assertThat(accountService.getAccountByName("Kosmetyki & Chemia").get().getTransactions().size()).isEqualTo(1)
        );
    }
}
