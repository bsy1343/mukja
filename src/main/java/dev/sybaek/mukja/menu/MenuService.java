// MenuService.java — 메뉴 조회 (카테고리 그룹 필터, 항목/옵션 lookup)
package dev.sybaek.mukja.menu;

import dev.sybaek.mukja.menu.domain.Category;
import dev.sybaek.mukja.menu.domain.MenuData;
import dev.sybaek.mukja.menu.domain.MenuItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {
    private final MenuRepository repo;
    public MenuService(MenuRepository repo) { this.repo = repo; }

    // 전체 메뉴 데이터
    public MenuData data() { return repo.load(); }

    // 카테고리 그룹(coffee/food)에 속한 서브카테고리만 반환
    public List<Category> categoriesIn(String group) {
        return repo.load().categories().stream().filter(c -> group.equals(c.group())).toList();
    }

    // 서브카테고리 id로 조회
    public Optional<Category> category(String subId) {
        return repo.load().categories().stream().filter(c -> c.id().equals(subId)).findFirst();
    }

    // 메뉴 항목 id로 조회
    public Optional<MenuItem> item(int itemId) {
        return repo.load().categories().stream()
                .flatMap(c -> c.items().stream())
                .filter(i -> i.id() == itemId).findFirst();
    }
}
