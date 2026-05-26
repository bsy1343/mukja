// OrderAggregator.java — 보드 주문을 메뉴별/사람별로 집계하고 요약 텍스트를 만든다
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.order.domain.*;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderAggregator {
    // (호환) 팀 명단 없이 집계한다
    public Aggregation aggregate(String place, String categoryName, String teamName, BoardData board) {
        return aggregate(place, categoryName, teamName, board, List.of());
    }

    // 장소·카테고리·팀 라벨, 보드 데이터, 팀 기대 명단(roster)으로 집계 결과를 만든다
    public Aggregation aggregate(String place, String categoryName, String teamName,
                                 BoardData board, List<String> roster) {
        var lines = board.orders().stream().flatMap(o -> o.lines().stream()).toList();

        // 메뉴별 집계 (등장 순서 유지)
        Map<String, MenuAcc> accs = new LinkedHashMap<>();
        for (var line : lines) {
            var acc = accs.computeIfAbsent(line.name(), k -> new MenuAcc());
            acc.count++;
            String opt = line.optionText().isBlank() ? "기본" : line.optionText();
            acc.breakdown.merge(opt, 1, Integer::sum);
        }
        List<MenuAgg> byMenu = accs.entrySet().stream()
                .map(en -> new MenuAgg(en.getKey(), en.getValue().count, en.getValue().breakdown)).toList();

        // 사람별 집계
        Map<String, List<OrderLine>> byPerson = new LinkedHashMap<>();
        for (var o : board.orders()) byPerson.put(o.person(), o.lines());

        int total = lines.stream().mapToInt(OrderLine::lineTotal).sum();
        int people = board.orders().size();
        Stats stats = new Stats(people, lines.size(), total, people == 0 ? 0 : total / people);

        // 미주문자: 명단 − 주문한 이름(공백 제거 후 정확 일치), 명단 순서 유지
        Set<String> ordered = board.orders().stream()
                .map(o -> o.person() == null ? "" : o.person().trim())
                .collect(Collectors.toSet());
        List<String> roaster = roster == null ? List.of() : roster;
        List<String> missing = roaster.stream()
                .filter(n -> n != null && !n.isBlank())
                .filter(n -> !ordered.contains(n.trim())).toList();
        int expected = (int) roaster.stream().filter(n -> n != null && !n.isBlank()).count();

        return new Aggregation(byMenu, byPerson, stats,
                summary(place, categoryName, teamName, byMenu, stats, missing, expected),
                missing, expected);
    }

    // 복사용 요약 텍스트 생성 (명단이 있으면 미주문자 줄 추가)
    private String summary(String place, String categoryName, String teamName,
                           List<MenuAgg> byMenu, Stats stats, List<String> missing, int expected) {
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(place).append(" · ").append(categoryName)
          .append(" · ").append(teamName).append("]\n");
        for (var m : byMenu) {
            String detail = m.optionBreakdown().entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(", "));
            sb.append("· ").append(m.name()).append(" ").append(m.totalCount()).append("잔 (")
              .append(detail).append(")\n");
        }
        sb.append("합계 ").append(nf.format(stats.totalAmount())).append("원 · ")
          .append(stats.people()).append("명");
        if (expected > 0) {
            sb.append("\n");
            if (missing.isEmpty()) sb.append("미주문 0명 (전원 주문)");
            else sb.append("미주문 ").append(missing.size()).append("명: ")
                   .append(String.join(", ", missing));
        }
        return sb.toString();
    }

    // 메뉴 집계용 가변 누산기
    private static class MenuAcc {
        int count = 0;
        Map<String, Integer> breakdown = new LinkedHashMap<>();
    }
}
