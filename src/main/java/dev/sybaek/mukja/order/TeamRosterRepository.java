// TeamRosterRepository.java — data/teams.json 로드 + 최초 seed 복사. 팀별 기대 명단 제공
package dev.sybaek.mukja.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.common.store.JsonStore;
import dev.sybaek.mukja.config.MukjaProperties;
import dev.sybaek.mukja.order.domain.TeamRoster;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Repository
public class TeamRosterRepository {
    private final Path file;
    private final JsonStore<TeamRoster> store;

    // 설정의 data-dir 아래 teams.json을 다룬다
    @Autowired
    public TeamRosterRepository(MukjaProperties props, ObjectMapper mapper) {
        this(props.dataDir(), mapper);
    }

    // 테스트용 생성자
    TeamRosterRepository(String dataDir, ObjectMapper mapper) {
        this.file = Path.of(dataDir, "teams.json");
        this.store = new JsonStore<>(file, TeamRoster.class, mapper, () -> new TeamRoster(Map.of()));
    }

    // 기동 시 teams.json이 없으면 클래스패스 seed를 복사한다
    @PostConstruct
    public void init() {
        if (Files.exists(file)) return;
        try {
            Files.createDirectories(file.getParent());
            try (var in = new ClassPathResource("data/teams.seed.json").getInputStream()) {
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) { throw new RuntimeException("seed copy failed", e); }
    }

    // 팀의 기대 명단 (없으면 빈 목록)
    public List<String> membersOf(String team) {
        var members = store.read().members();
        return members == null ? List.of() : members.getOrDefault(team, List.of());
    }
}
