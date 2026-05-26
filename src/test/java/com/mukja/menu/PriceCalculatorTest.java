// PriceCalculatorTest.java — 데이터 주도 가격 계산 + 옵션 텍스트 검증
package com.mukja.menu;

import com.mukja.menu.domain.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {
    private final Map<String, OptionDef> defs = Map.of(
        "temp", new OptionDef("HOT / ICE", "single", true,
            List.of(new OptionChoice("hot","HOT",0), new OptionChoice("ice","ICE",500)), null, null),
        "shot", new OptionDef("샷 추가", "counter", false, null, 500, 3),
        "light", new OptionDef("연하게", "toggle", false, null, 0, null));

    private final MenuItem americano = new MenuItem(102, "아메리카노", 1600,
        List.of("temp","light","shot"), null);

    @Test
    void iceShotAddsExtras() {
        Map<String,Object> sel = Map.of("temp","ice", "shot", 1, "light", true);
        assertEquals(1600 + 500 + 500, new PriceCalculator().calc(americano, defs, sel));
    }

    @Test
    void hotNoExtra() {
        assertEquals(1600, new PriceCalculator().calc(americano, defs, Map.of("temp","hot")));
    }

    @Test
    void fixedTempIgnoresIceExtra() {
        MenuItem espresso = new MenuItem(101,"에스프레소",1500, List.of("shot"), "hot");
        assertEquals(1500, new PriceCalculator().calc(espresso, defs, Map.of("shot", 0)));
    }

    @Test
    void optionTextIsHumanReadable() {
        Map<String,Object> sel = new LinkedHashMap<>();
        sel.put("temp","ice"); sel.put("light", true); sel.put("shot", 1);
        assertEquals("ICE·연하게·샷+1", new OptionTextBuilder().build(americano, defs, sel));
    }
}
