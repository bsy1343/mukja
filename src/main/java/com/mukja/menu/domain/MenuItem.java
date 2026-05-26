// MenuItem.java — 메뉴 항목. fixedTemp가 있으면 temp 옵션을 해당 값으로 고정
package com.mukja.menu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuItem(int id, String name, int price, List<String> options, String fixedTemp) {}
