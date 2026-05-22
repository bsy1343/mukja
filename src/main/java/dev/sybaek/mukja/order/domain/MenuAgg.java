// MenuAgg.java — 메뉴별 집계 (총 잔수 + 옵션 분해)
package dev.sybaek.mukja.order.domain;
import java.util.Map;
public record MenuAgg(String name, int totalCount, Map<String, Integer> optionBreakdown) {}
