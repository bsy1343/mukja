// MenuData.java — 메뉴 루트 (장소, 옵션정의 맵, 카테고리 목록)
package dev.sybaek.mukja.menu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuData(Place place, Map<String, OptionDef> optionDefs, List<Category> categories) {}
