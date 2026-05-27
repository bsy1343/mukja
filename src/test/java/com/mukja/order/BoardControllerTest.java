// BoardControllerTest.java — 주문판 화면과 메뉴 그리드/옵션 fragment 검증
package com.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void boardPageShowsTeamAndMembers() throws Exception {
        mvc.perform(get("/coffee/kt/ice")).andExpect(status().isOk())
           .andExpect(content().string(containsString("ICE")))
           .andExpect(content().string(containsString("김자영")));
    }

    @Test
    void menuGridFragmentListsItems() throws Exception {
        mvc.perform(get("/coffee/kt/ice/menu").param("cat", "coffee")).andExpect(status().isOk())
           .andExpect(content().string(containsString("아메리카노")));
    }

    @Test
    void optionModalFragmentShowsOptions() throws Exception {
        mvc.perform(get("/coffee/kt/ice/menu/102/options")).andExpect(status().isOk())
           .andExpect(content().string(containsString("HOT")));
    }

    @Test
    void blankPersonIsRejected() throws Exception {
        mvc.perform(post("/coffee/kt/ice/orders").contentType("application/json")
                .content("{\"person\":\"   \",\"lines\":[{\"itemId\":102,\"options\":{\"temp\":\"hot\"}}]}"))
           .andExpect(status().isBadRequest());
    }
}
