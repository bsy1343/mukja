// MenuService.java — 메뉴 조회 (가게 그룹 필터, 가게/카테고리/항목 lookup)
package com.mukja.menu;

import com.mukja.menu.domain.Category;
import com.mukja.menu.domain.MenuData;
import com.mukja.menu.domain.MenuItem;
import com.mukja.menu.domain.Vendor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MenuService {
    private final MenuRepository repo;
    public MenuService(MenuRepository repo) { this.repo = repo; }

    // 전체 메뉴 데이터
    public MenuData data() { return repo.load(); }

    // 그룹(coffee/food)에 속한 가게 목록 (이름 오름차순)
    public List<Vendor> vendorsIn(String group) {
        return repo.load().vendors().stream()
                .filter(v -> group.equals(v.group()))
                .sorted(Comparator.comparing(Vendor::name)).toList();
    }

    // 가게 id로 조회
    public Optional<Vendor> vendor(String vendorId) {
        return repo.load().vendors().stream().filter(v -> v.id().equals(vendorId)).findFirst();
    }

    // 가게의 메뉴 카테고리(탭)
    public List<Category> categoriesOf(String vendorId) {
        return vendor(vendorId).map(Vendor::categories).orElse(List.of());
    }

    // 서브카테고리 id로 조회 (전 가게 통합)
    public Optional<Category> category(String subId) {
        return repo.load().vendors().stream().flatMap(v -> v.categories().stream())
                .filter(c -> c.id().equals(subId)).findFirst();
    }

    // 메뉴 항목 id로 조회 (전 가게 통합)
    public Optional<MenuItem> item(int itemId) {
        return repo.load().vendors().stream().flatMap(v -> v.categories().stream())
                .flatMap(c -> c.items().stream())
                .filter(i -> i.id() == itemId).findFirst();
    }
}
