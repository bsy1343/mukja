// StatusControllerTest.java — 집계 화면 및 summary.txt 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatusControllerTest {
    @Autowired MockMvc mvc;
    @Autowired OrderService service;
    @Autowired OrderRepository repo;

    @BeforeEach
    void seed() {
        repo.reset("coffee", "stat-team");
        service.submit("coffee", "stat-team", "백상열",
            java.util.List.of(new OrderService.LineInput(102, java.util.Map.of("temp", "ice"))));
    }

    @AfterEach
    void clean() { repo.reset("coffee", "stat-team"); }

    @Test
    void statusPageShowsStats() throws Exception {
        mvc.perform(get("/coffee/stat-team/status")).andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")));
    }

    @Test
    void summaryTxtIsPlainText() throws Exception {
        mvc.perform(get("/coffee/stat-team/status/summary.txt")).andExpect(status().isOk())
           .andExpect(content().string(containsString("[KT")))
           .andExpect(content().string(containsString("1명")));
    }

    @Test
    void statusFragmentForHtmxRequest() throws Exception {
        mvc.perform(get("/coffee/stat-team/status").header("HX-Request", "true"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")))
           .andExpect(content().string(org.hamcrest.Matchers.not(containsString("<html"))));
    }
}
