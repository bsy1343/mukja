// MenuRepositoryTest.java — seed 복사 및 로드 검증
package dev.sybaek.mukja.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sybaek.mukja.menu.domain.MenuData;
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
        assertEquals(2, data.categories().size());
    }

    @Test
    void keepsExistingMenusJson(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("menus.json"),
            "{\"place\":{\"id\":\"x\",\"name\":\"X\",\"floor\":\"1\"},\"optionDefs\":{},\"categories\":[]}");
        MenuRepository repo = new MenuRepository(dir.toString(), new ObjectMapper());
        repo.init();
        assertEquals("x", repo.load().place().id());
    }
}
