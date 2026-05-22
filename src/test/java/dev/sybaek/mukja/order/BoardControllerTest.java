// BoardControllerTest.java — 주문판 화면과 메뉴 그리드/옵션 fragment 검증
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
class BoardControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void boardPageShowsHeader() throws Exception {
        mvc.perform(get("/coffee/sa")).andExpect(status().isOk())
           .andExpect(content().string(containsString("SA팀")));
    }

    @Test
    void menuGridFragmentListsItems() throws Exception {
        mvc.perform(get("/coffee/sa/menu").param("cat", "coffee")).andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")));
    }

    @Test
    void optionModalFragmentShowsOptions() throws Exception {
        mvc.perform(get("/coffee/sa/menu/102/options")).andExpect(status().isOk())
           .andExpect(content().string(containsString("HOT")));
    }
}
