// PriceCalculator.java — 메뉴 가격 + 선택 옵션의 가산금액 합산 (데이터 주도)
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.MenuItem;
import dev.sybaek.mukja.menu.domain.OptionChoice;
import dev.sybaek.mukja.menu.domain.OptionDef;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PriceCalculator {
    // item 기본가 + 선택 옵션 가산금액을 합산한다
    public int calc(MenuItem item, Map<String, OptionDef> defs, Map<String, Object> selected) {
        int total = item.price();
        for (var entry : selected.entrySet()) {
            OptionDef def = defs.get(entry.getKey());
            if (def == null) continue;
            total += extraFor(item, entry.getKey(), def, entry.getValue());
        }
        return total;
    }

    // 옵션 한 개의 가산금액을 계산한다
    private int extraFor(MenuItem item, String key, OptionDef def, Object value) {
        return switch (def.type()) {
            case "single" -> {
                // 온도가 고정된 메뉴는 temp 가산금액을 무시한다
                if ("temp".equals(key) && item.fixedTemp() != null) yield 0;
                yield def.choices().stream()
                        .filter(c -> c.id().equals(String.valueOf(value)))
                        .mapToInt(OptionChoice::extra).findFirst().orElse(0);
            }
            case "toggle" -> Boolean.TRUE.equals(value) ? def.extra() : 0;
            case "counter" -> ((Number) value).intValue() * def.extra();
            default -> 0;
        };
    }
}
