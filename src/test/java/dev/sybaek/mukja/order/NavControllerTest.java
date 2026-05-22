// NavControllerTest.java — 카테고리/팀 선택 화면 렌더 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NavControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void categoryPageLists() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("커피")));
    }

    @Test
    void teamPageListsTeams() throws Exception {
        mvc.perform(get("/coffee")).andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("전체")))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("SA팀")));
    }
}
