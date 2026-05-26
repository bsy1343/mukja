// MenuRepository.java — menus.json 로드/저장 + 최초 seed 복사
package com.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukja.common.store.JsonStore;
import com.mukja.config.MukjaProperties;
import com.mukja.menu.domain.MenuData;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Repository
public class MenuRepository {
    private final Path file;
    private final JsonStore<MenuData> store;
    private final ObjectMapper mapper;

    // 설정의 data-dir 아래 menus.json을 다룬다
    @Autowired
    public MenuRepository(MukjaProperties props, ObjectMapper mapper) {
        this(props.dataDir(), mapper);
    }

    // 테스트용 생성자
    MenuRepository(String dataDir, ObjectMapper mapper) {
        this.file = Path.of(dataDir, "menus.json");
        this.mapper = mapper;
        this.store = new JsonStore<>(file, MenuData.class, mapper,
                () -> new MenuData(Map.of(), List.of()));
    }

    // 기동 시 menus.json이 없으면 클래스패스 seed를 복사한다
    @PostConstruct
    public void init() {
        if (Files.exists(file)) return;
        try {
            Files.createDirectories(file.getParent());
            try (var in = new ClassPathResource("data/menus.seed.json").getInputStream()) {
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) { throw new RuntimeException("seed copy failed", e); }
    }

    // 현재 메뉴 데이터를 읽는다
    public MenuData load() { return store.read(); }

    // 메뉴 데이터를 저장한다 (관리자 CRUD)
    public void save(MenuData data) { store.write(data); }
}
