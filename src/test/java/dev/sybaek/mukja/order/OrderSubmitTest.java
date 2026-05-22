// OrderSubmitTest.java — 주문 제출 시 가격 계산·옵션텍스트·저장 검증
package dev.sybaek.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderSubmitTest {
    @Autowired OrderService service;
    @Autowired OrderRepository repo;

    @Test
    void submitComputesLineTotalAndText() {
        repo.reset("coffee", "test-team");
        var line = new OrderService.LineInput(102, Map.of("temp", "ice", "shot", 1));
        service.submit("coffee", "test-team", "백상열", List.of(line));
        var board = repo.read("coffee", "test-team");
        assertEquals(1, board.orders().size());
        var saved = board.orders().get(0).lines().get(0);
        assertEquals(1600 + 500 + 500, saved.lineTotal());
        assertTrue(saved.optionText().contains("ICE"));
        repo.reset("coffee", "test-team");
    }
}
