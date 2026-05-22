// OptionTextBuilder.java — 선택 옵션을 "ICE·연하게·샷+1" 형태 텍스트로 만든다
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.MenuItem;
import dev.sybaek.mukja.menu.domain.OptionDef;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OptionTextBuilder {
    // 메뉴의 options 순서대로 선택값을 사람이 읽는 텍스트로 변환한다
    public String build(MenuItem item, Map<String, OptionDef> defs, Map<String, Object> selected) {
        List<String> parts = new ArrayList<>();
        for (String key : item.options()) {
            OptionDef def = defs.get(key);
            Object value = selected.get(key);
            if (def == null || value == null) continue;
            switch (def.type()) {
                case "single" -> def.choices().stream()
                        .filter(c -> c.id().equals(String.valueOf(value)))
                        .findFirst().ifPresent(c -> { if (c.extra() >= 0) parts.add(c.name()); });
                case "toggle" -> { if (Boolean.TRUE.equals(value)) parts.add(def.label()); }
                case "counter" -> {
                    int n = ((Number) value).intValue();
                    if (n > 0) parts.add("샷+" + n);
                }
            }
        }
        return String.join("·", parts);
    }
}
