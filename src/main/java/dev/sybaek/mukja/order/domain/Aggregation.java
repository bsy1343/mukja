// Aggregation.java — 집계 결과 묶음 (missing: 미주문자 명단, expected: 기대 명단 인원수)
package dev.sybaek.mukja.order.domain;
import java.util.List;
import java.util.Map;
public record Aggregation(List<MenuAgg> byMenu, Map<String, List<OrderLine>> byPerson,
                          Stats stats, String summaryText,
                          List<String> missing, int expected) {}
