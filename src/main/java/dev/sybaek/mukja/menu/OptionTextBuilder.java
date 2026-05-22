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
                // 매칭된 단일선택 보기는 가산금액과 무관하게 모두 표시한다 (예: 얼음량 '기본'도 노출)
                case "single" -> def.choices().stream()
                        .filter(c -> c.id().equals(String.valueOf(value)))
                        .findFirst().ifPresent(c -> parts.add(c.name()));
                case "toggle" -> { if (Boolean.TRUE.equals(value)) parts.add(def.label()); }
                case "counter" -> {
                    int n = ((Number) value).intValue();
                    // NOTE: 현재 counter 옵션은 '샷'뿐이라 라벨을 하드코딩한다.
                    // 다른 counter 옵션이 추가되면 OptionDef에 표시용 라벨을 두고 데이터 주도로 바꿀 것.
                    if (n > 0) parts.add("샷+" + n);
                }
            }
        }
        return String.join("·", parts);
    }
}
