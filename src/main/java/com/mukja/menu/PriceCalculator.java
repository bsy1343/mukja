// PriceCalculator.java — 메뉴 가격 + 선택 옵션의 가산금액 합산 (데이터 주도)
package com.mukja.menu;

import com.mukja.menu.domain.MenuItem;
import com.mukja.menu.domain.OptionChoice;
import com.mukja.menu.domain.OptionDef;
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
            case "toggle" -> (Boolean.TRUE.equals(value) && def.extra() != null) ? def.extra() : 0;
            case "counter" -> intOf(value) * (def.extra() == null ? 0 : def.extra());
            default -> 0;
        };
    }

    // counter 값은 클라이언트에 따라 숫자(1) 또는 문자열("1")로 올 수 있어 안전 변환한다
    static int intOf(Object value) {
        if (value instanceof Number n) return n.intValue();
        try { return value == null ? 0 : Integer.parseInt(value.toString().trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
