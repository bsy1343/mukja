// OrderRepositoryTest.java — 보드 제출/덮어쓰기/마감차단/초기화 검증
package com.mukja.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mukja.order.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {
    private OrderRepository repo(Path dir) {
        return new OrderRepository(dir.toString(), new ObjectMapper().registerModule(new JavaTimeModule()));
    }
    private OrderEntry entry(String person, int total) {
        return new OrderEntry(person, OffsetDateTime.now(),
            List.of(new OrderLine(102,"아메리카노",1600, java.util.Map.of("temp","hot"), "HOT", total)));
    }

    @Test
    void submitAndRead(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        assertEquals(1, repo.read("coffee","sa").orders().size());
    }

    @Test
    void resubmitOverwritesSamePerson(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        repo.submit("coffee","sa", entry("백상열", 2100));
        BoardData b = repo.read("coffee","sa");
        assertEquals(1, b.orders().size());
        assertEquals(2100, b.orders().get(0).lines().get(0).lineTotal());
    }

    @Test
    void boardsAreIsolated(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        assertEquals(0, repo.read("coffee","imdg").orders().size());
    }

    @Test
    void removePersonDeletesOnlyThatOrder(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        repo.submit("coffee","sa", entry("홍길동", 2100));
        repo.removePerson("coffee","sa", "백상열");
        BoardData b = repo.read("coffee","sa");
        assertEquals(1, b.orders().size());
        assertEquals("홍길동", b.orders().get(0).person());
    }

    @Test
    void submitAfterDeadlineThrows(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.setDeadline("coffee","sa", OffsetDateTime.now().minusMinutes(1));
        assertThrows(BoardClosedException.class, () -> repo.submit("coffee","sa", entry("백상열", 1600)));
    }

    @Test
    void resetClearsOrdersAndDeadline(@TempDir Path dir) {
        OrderRepository repo = repo(dir);
        repo.submit("coffee","sa", entry("백상열", 1600));
        repo.setDeadline("coffee","sa", OffsetDateTime.now().plusMinutes(30));
        repo.reset("coffee","sa");
        BoardData b = repo.read("coffee","sa");
        assertTrue(b.orders().isEmpty());
        assertNull(b.closeAt());
    }
}
