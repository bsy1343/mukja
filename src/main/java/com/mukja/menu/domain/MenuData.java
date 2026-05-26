// MenuData.java — 메뉴 루트 (옵션정의 맵, 가게(vendor) 목록)
package com.mukja.menu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuData(Map<String, OptionDef> optionDefs, List<Vendor> vendors) {}
