// MenuDataJsonTest.java — 시드 JSON이 도메인으로 정상 매핑되는지 검증
package com.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukja.menu.domain.MenuData;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class MenuDataJsonTest {
    @Test
    void seedJsonDeserializes() throws Exception {
        MenuData data = new ObjectMapper().readValue(
                new ClassPathResource("data/menus.seed.json").getInputStream(), MenuData.class);
        assertEquals(9, data.vendors().size()); // KT + 식당 8개(고향집·두향·란반·밥상머리·푸른바다·예돈·라이라이·등촌)
        var kt = data.vendors().get(0);
        assertEquals("kt", kt.id());
        assertEquals("coffee", kt.group());
        assertEquals(6, kt.categories().size());
        assertEquals("hot", kt.categories().get(1).items().get(0).fixedTemp()); // 디카페인 에스프레소
        assertTrue(data.optionDefs().containsKey("temp"));
        assertEquals(500, data.optionDefs().get("shot").extra());
    }
}
