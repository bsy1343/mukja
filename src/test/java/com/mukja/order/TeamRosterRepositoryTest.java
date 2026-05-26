// TeamRosterRepositoryTest.java — seed 복사 + 팀 명단 조회 검증
package com.mukja.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamRosterRepositoryTest {

    @Test
    void copiesSeedWhenMissingThenEmptyRoster(@TempDir Path dir) {
        var repo = new TeamRosterRepository(dir.toString(), new ObjectMapper());
        repo.init();
        assertTrue(Files.exists(dir.resolve("teams.json")));
        assertTrue(repo.membersOf("sa").isEmpty()); // 시드는 빈 템플릿
    }

    @Test
    void readsConfiguredMembersAndKeepsExistingFile(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("teams.json"),
            "{\"members\":{\"sa\":[\"백상열\",\"홍길동\"]}}");
        var repo = new TeamRosterRepository(dir.toString(), new ObjectMapper());
        repo.init(); // 파일이 있으면 시드하지 않음
        assertEquals(List.of("백상열", "홍길동"), repo.membersOf("sa"));
        assertTrue(repo.membersOf("unknown").isEmpty());
    }
}
