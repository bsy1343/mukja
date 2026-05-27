// MidnightResetTest.java — 자정 스케줄러가 (가게×팀) 보드를 비우는지 검증
package com.mukja.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MidnightResetTest {
    @Autowired BoardResetScheduler scheduler;
    @Autowired OrderRepository repo;
    @Autowired OrderService service;

    @Test
    void midnightResetClearsBoards() {
        repo.reset("kt", "ice");
        service.submit("kt", "ice", "백상열",
            List.of(new OrderService.LineInput(102, Map.of("temp", "hot"))));
        assertEquals(1, repo.read("kt", "ice").orders().size());

        scheduler.resetAllBoardsAtMidnight();

        assertTrue(repo.read("kt", "ice").orders().isEmpty());
    }
}
