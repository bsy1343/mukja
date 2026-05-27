// OptionTextBuilder.java — 선택 옵션을 "ICE·연하게·샷+1" 형태 텍스트로 만든다
package com.mukja.menu;

import com.mukja.menu.domain.MenuItem;
import com.mukja.menu.domain.OptionDef;
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
                // 단일선택: 매칭 보기를 표시. 단, 비필수 옵션의 첫 보기(기본값, 예: 얼음 '기본')는 군더더기라 생략
                case "single" -> {
                    var choices = def.choices();
                    for (int i = 0; i < choices.size(); i++) {
                        if (choices.get(i).id().equals(String.valueOf(value))) {
                            if (def.required() || i != 0) parts.add(choices.get(i).name());
                            break;
                        }
                    }
                }
                case "toggle" -> { if (Boolean.TRUE.equals(value)) parts.add(def.label()); }
                case "counter" -> {
                    int n = PriceCalculator.intOf(value); // 숫자/문자열 모두 안전 변환
                    // NOTE: 현재 counter 옵션은 '샷'뿐이라 라벨을 하드코딩한다.
                    // 다른 counter 옵션이 추가되면 OptionDef에 표시용 라벨을 두고 데이터 주도로 바꿀 것.
                    if (n > 0) parts.add("샷+" + n);
                }
            }
        }
        return String.join("·", parts);
    }
}
