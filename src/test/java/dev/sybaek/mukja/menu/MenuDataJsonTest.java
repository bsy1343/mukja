// MenuDataJsonTest.java — 시드 JSON이 도메인으로 정상 매핑되는지 검증
package dev.sybaek.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.menu.domain.MenuData;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class MenuDataJsonTest {
    @Test
    void seedJsonDeserializes() throws Exception {
        MenuData data = new ObjectMapper().readValue(
                new ClassPathResource("data/menus.seed.json").getInputStream(), MenuData.class);
        assertEquals("kt-bundang-cafe", data.place().id());
        assertEquals(6, data.categories().size());
        assertEquals("coffee", data.categories().get(0).group());
        assertEquals("hot", data.categories().get(1).items().get(0).fixedTemp());
        assertTrue(data.optionDefs().containsKey("temp"));
        assertEquals(500, data.optionDefs().get("shot").extra());
    }
}
