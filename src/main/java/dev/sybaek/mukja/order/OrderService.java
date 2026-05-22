// OrderService.java — 주문 제출 (가격 계산 + 옵션 텍스트 + 저장)
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.menu.MenuService;
import dev.sybaek.mukja.menu.OptionTextBuilder;
import dev.sybaek.mukja.menu.PriceCalculator;
import dev.sybaek.mukja.menu.domain.MenuItem;
import dev.sybaek.mukja.order.domain.OrderEntry;
import dev.sybaek.mukja.order.domain.OrderLine;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository repo;
    private final MenuService menu;
    private final PriceCalculator price;
    private final OptionTextBuilder optionText;

    public OrderService(OrderRepository repo, MenuService menu,
                        PriceCalculator price, OptionTextBuilder optionText) {
        this.repo = repo; this.menu = menu; this.price = price; this.optionText = optionText;
    }

    // 한 줄 주문 입력 (itemId + 선택옵션)
    public record LineInput(int itemId, Map<String, Object> options) {}

    // 주문을 가격 계산·옵션텍스트와 함께 보드에 제출한다
    public void submit(String category, String team, String person, List<LineInput> inputs) {
        var defs = menu.data().optionDefs();
        List<OrderLine> lines = inputs.stream().map(in -> {
            MenuItem item = menu.item(in.itemId()).orElseThrow();
            int total = price.calc(item, defs, in.options());
            String text = optionText.build(item, defs, in.options());
            return new OrderLine(item.id(), item.name(), item.price(), in.options(), text, total);
        }).toList();
        repo.submit(category, team, new OrderEntry(person, OffsetDateTime.now(), lines));
    }
}
