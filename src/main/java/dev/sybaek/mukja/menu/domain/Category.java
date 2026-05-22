// Category.java — 서브카테고리. group: coffee|food
package dev.sybaek.mukja.menu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Category(String id, String name, String group, List<MenuItem> items) {}
