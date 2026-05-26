// Vendor.java — 가게/상호. 상위 그룹(coffee/food)에 속하며 자체 메뉴 카테고리를 가진다
package com.mukja.menu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Vendor(String id, String name, String group, String floor, List<Category> categories) {}
