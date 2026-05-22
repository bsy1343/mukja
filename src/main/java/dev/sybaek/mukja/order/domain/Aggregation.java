// Aggregation.java — 집계 결과 묶음
package dev.sybaek.mukja.order.domain;
import java.util.List;
import java.util.Map;
public record Aggregation(List<MenuAgg> byMenu, Map<String, List<OrderLine>> byPerson,
                          Stats stats, String summaryText) {}
