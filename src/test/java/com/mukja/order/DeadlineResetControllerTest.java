// DeadlineResetControllerTest.java — 마감 설정/해제/초기화 엔드포인트 검증
package com.mukja.order;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DeadlineResetControllerTest {
    @Autowired MockMvc mvc;
    @Autowired OrderRepository repo;

    @AfterEach
    void clean() { repo.reset("kt", "dl-team"); }

    @Test
    void setDeadlineThenClear() throws Exception {
        mvc.perform(post("/coffee/kt/dl-team/deadline").contentType("application/json")
                .content("{\"time\":\"14:30\"}")).andExpect(status().isOk());
        Assertions.assertNotNull(repo.read("kt", "dl-team").closeAt());
        mvc.perform(post("/coffee/kt/dl-team/deadline").contentType("application/json")
                .content("{\"time\":null}")).andExpect(status().isOk());
        Assertions.assertNull(repo.read("kt", "dl-team").closeAt());
    }

    @Test
    void resetClearsBoard() throws Exception {
        mvc.perform(post("/coffee/kt/dl-team/reset")).andExpect(status().isOk());
        Assertions.assertTrue(repo.read("kt", "dl-team").orders().isEmpty());
    }
}
