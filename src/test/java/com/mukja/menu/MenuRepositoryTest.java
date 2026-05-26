// MenuRepositoryTest.java — seed 복사 및 로드 검증
package com.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukja.menu.domain.MenuData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuRepositoryTest {
    @Test
    void copiesSeedWhenMissingThenLoads(@TempDir Path dir) {
        MenuRepository repo = new MenuRepository(dir.toString(), new ObjectMapper());
        repo.init();
        assertTrue(Files.exists(dir.resolve("menus.json")));
        MenuData data = repo.load();
        assertEquals(6, data.vendors().size()); // KT + 식당 5개
    }

    @Test
    void keepsExistingMenusJson(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("menus.json"),
            "{\"optionDefs\":{},\"vendors\":[{\"id\":\"x\",\"name\":\"X\",\"group\":\"food\",\"categories\":[]}]}");
        MenuRepository repo = new MenuRepository(dir.toString(), new ObjectMapper());
        repo.init();
        assertEquals("x", repo.load().vendors().get(0).id());
    }
}
