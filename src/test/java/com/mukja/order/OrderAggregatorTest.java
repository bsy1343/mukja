// OrderAggregatorTest.java — 메뉴별/사람별 집계 + 요약 텍스트 검증
package com.mukja.order;

import com.mukja.order.domain.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderAggregatorTest {
    private OrderEntry e(String p, OrderLine... lines) {
        return new OrderEntry(p, OffsetDateTime.now(), List.of(lines));
    }
    private OrderLine l(String name, String optText, int total) {
        return new OrderLine(1, name, total, Map.of(), optText, total);
    }

    @Test
    void aggregatesByMenuAndPersonAndStats() {
        var board = new BoardData(null, List.of(
            e("백상열", l("아메리카노", "ICE", 1600), l("카페라떼", "HOT", 2100)),
            e("김철수", l("아메리카노", "HOT", 1600))));
        var agg = new OrderAggregator().aggregate("KT 분당 카페", "커피", "SA팀", board);

        assertEquals(2, agg.stats().people());
        assertEquals(3, agg.stats().cups());
        assertEquals(5300, agg.stats().totalAmount());
        var americano = agg.byMenu().stream().filter(m -> m.name().equals("아메리카노")).findFirst().orElseThrow();
        assertEquals(2, americano.totalCount());
        assertEquals(1, americano.optionBreakdown().get("ICE"));
        assertEquals(1, americano.optionBreakdown().get("HOT"));
        assertEquals(2, agg.byPerson().get("백상열").size());
        assertTrue(agg.summaryText().contains("[KT 분당 카페 · 커피 · SA팀]"));
        assertFalse(agg.summaryText().contains("8명"));
        assertEquals(0, agg.expected());
        assertTrue(agg.missing().isEmpty());
    }

    @Test
    void detectsMissingFromRoster() {
        var board = new BoardData(null, List.of(
            e("백상열", l("아메리카노", "ICE", 1600)),
            e(" 김철수 ", l("카페라떼", "HOT", 2100)))); // 공백 포함 → trim 후 일치
        var roster = List.of("백상열", "김철수", "홍길동", "이영희");
        var agg = new OrderAggregator().aggregate("KT", "커피", "SA팀", board, roster);

        assertEquals(4, agg.expected());
        assertEquals(List.of("홍길동", "이영희"), agg.missing()); // 명단 순서 유지
        assertTrue(agg.summaryText().contains("미주문 2명: 홍길동, 이영희"));
    }

    @Test
    void allOrderedShowsNoMissing() {
        var board = new BoardData(null, List.of(e("백상열", l("아메리카노", "ICE", 1600))));
        var agg = new OrderAggregator().aggregate("KT", "커피", "SA팀", board, List.of("백상열"));
        assertEquals(1, agg.expected());
        assertTrue(agg.missing().isEmpty());
        assertTrue(agg.summaryText().contains("미주문 0명 (전원 주문)"));
    }
}
