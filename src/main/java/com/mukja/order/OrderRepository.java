// OrderRepository.java — 보드별(카테고리×팀) 주문 저장. 보드마다 독립 JsonStore
package com.mukja.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukja.common.store.JsonStore;
import com.mukja.config.MukjaProperties;
import com.mukja.order.domain.BoardData;
import com.mukja.order.domain.OrderEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {
    private final String dataDir;
    private final ObjectMapper mapper;
    private final Map<String, JsonStore<BoardData>> stores = new ConcurrentHashMap<>();

    // 설정의 data-dir 아래 orders/ 디렉토리를 사용한다
    @Autowired
    public OrderRepository(MukjaProperties props, ObjectMapper mapper) {
        this(props.dataDir(), mapper);
    }

    // 테스트용 생성자
    OrderRepository(String dataDir, ObjectMapper mapper) {
        this.dataDir = dataDir;
        this.mapper = mapper;
    }

    // 보드별 JsonStore를 lazy 생성/캐시한다
    private JsonStore<BoardData> store(String category, String team) {
        String key = category + "-" + team;
        return stores.computeIfAbsent(key, k -> new JsonStore<>(
                Path.of(dataDir, "orders", k + ".json"), BoardData.class, mapper, BoardData::empty));
    }

    // 보드 데이터를 읽는다
    public BoardData read(String category, String team) { return store(category, team).read(); }

    // 주문을 제출/수정한다. 동일 person은 덮어쓴다. 마감 경과 시 예외
    public void submit(String category, String team, OrderEntry entry) {
        store(category, team).mutate(cur -> {
            if (cur.closeAt() != null && OffsetDateTime.now().isAfter(cur.closeAt()))
                throw new BoardClosedException("마감된 주문판입니다");
            List<OrderEntry> next = new ArrayList<>(cur.orders());
            next.removeIf(e -> e.person().equals(entry.person()));
            next.add(entry);
            return new BoardData(cur.closeAt(), next);
        });
    }

    // 마감 시각을 설정한다
    public void setDeadline(String category, String team, OffsetDateTime closeAt) {
        store(category, team).mutate(cur -> new BoardData(closeAt, new ArrayList<>(cur.orders())));
    }

    // 마감 시각을 해제한다
    public void clearDeadline(String category, String team) {
        store(category, team).mutate(cur -> new BoardData(null, new ArrayList<>(cur.orders())));
    }

    // 특정 person의 주문만 삭제한다 (주문 취소)
    public void removePerson(String category, String team, String person) {
        store(category, team).mutate(cur -> {
            List<OrderEntry> next = new ArrayList<>(cur.orders());
            next.removeIf(e -> e.person().equals(person));
            return new BoardData(cur.closeAt(), next);
        });
    }

    // 주문 내역과 마감을 모두 비운다 (재사용)
    public void reset(String category, String team) {
        store(category, team).write(BoardData.empty());
    }
}
