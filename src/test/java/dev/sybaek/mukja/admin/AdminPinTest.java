// AdminPinTest.java — PIN 없이 /admin 접근 차단, 올바른 PIN 후 접근 허용
package dev.sybaek.mukja.admin;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPinTest {
    @Autowired MockMvc mvc;

    @Test
    void redirectsToLoginWithoutPin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
    }

    @Test
    void allowsWithValidPinCookie() throws Exception {
        mvc.perform(get("/admin").cookie(new Cookie("admin", "1234")))
           .andExpect(status().isOk());
    }
}
