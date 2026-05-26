// NavControllerTest.java — 루트/카테고리 리다이렉트 + 보드 진입 검증
package com.mukja.order;

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
    void rootRedirectsToDefaultVendorBoard() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/coffee/kt/ice"));
    }

    @Test
    void categoryRedirectsToFirstVendor() throws Exception {
        mvc.perform(get("/coffee")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/coffee/kt/ice"));
    }

    @Test
    void boardListsTeamsVendorsMembersAndMenu() throws Exception {
        mvc.perform(get("/coffee/kt/ice")).andExpect(status().isOk())
           .andExpect(content().string(containsString("ICE")))
           .andExpect(content().string(containsString("비발디")))
           .andExpect(content().string(containsString("김자영")))   // ICE 팀원
           .andExpect(content().string(containsString("KT그룹희망나눔재단")))
           .andExpect(content().string(containsString("고향집삼계탕")))
           .andExpect(content().string(containsString("커피")));
    }
}
