// NavControllerTest.java — 루트/카테고리 리다이렉트 + 보드 진입 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NavControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void rootRedirectsToDefaultBoard() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/coffee/all"));
    }

    @Test
    void categoryRedirectsToDefaultTeam() throws Exception {
        mvc.perform(get("/coffee")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/coffee/all"));
    }

    @Test
    void boardListsTeamsAndCategories() throws Exception {
        mvc.perform(get("/coffee/all")).andExpect(status().isOk())
           .andExpect(content().string(containsString("전체")))
           .andExpect(content().string(containsString("SA팀")))
           .andExpect(content().string(containsString("커피")));
    }
}
