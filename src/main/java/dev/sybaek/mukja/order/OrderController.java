// OrderController.java — 네비게이션/주문/상태 라우트
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.config.MukjaProperties;
import dev.sybaek.mukja.menu.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {
    private final MukjaProperties props;
    private final MenuService menuService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderController(MukjaProperties props, MenuService menuService,
                           OrderRepository orderRepository, OrderService orderService) {
        this.props = props;
        this.menuService = menuService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    // 카테고리 선택 화면
    @GetMapping("/")
    public String category() { return "order/category"; }

    // 팀 선택 화면 (정규식으로 알려진 카테고리만 매핑 — static 경로 보호)
    @GetMapping("/{category:coffee|food}")
    public String team(@PathVariable String category, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("categoryName", "커피");
        model.addAttribute("teams", props.teams());
        return "order/team";
    }

    // 팀 표시 이름 조회 (없으면 id 그대로)
    private String teamName(String teamId) {
        return props.teams().stream().filter(t -> t.id().equals(teamId))
                .map(MukjaProperties.Team::name).findFirst().orElse(teamId);
    }

    // 주문판 화면
    @GetMapping("/{category:coffee|food}/{team}")
    public String board(@PathVariable String category, @PathVariable String team, Model model) {
        var board = orderRepository.read(category, team);
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("subCategories", menuService.categoriesIn(category));
        model.addAttribute("closeAt", board.closeAt());
        return "order/board";
    }

    // 서브카테고리 메뉴 그리드 fragment
    @GetMapping("/{category:coffee|food}/{team}/menu")
    public String menuGrid(@PathVariable String category, @PathVariable String team,
                           @RequestParam String cat, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("subCategory", menuService.category(cat).orElseThrow());
        return "order/fragments/menu-grid :: grid";
    }

    // 옵션 선택 모달 fragment
    @GetMapping("/{category:coffee|food}/{team}/menu/{itemId}/options")
    public String optionModal(@PathVariable String category, @PathVariable String team,
                              @PathVariable int itemId, Model model) {
        var item = menuService.item(itemId).orElseThrow();
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("item", item);
        model.addAttribute("optionDefs", menuService.data().optionDefs());
        return "order/fragments/option-modal :: modal";
    }

    // 주문 제출/수정 (HTMX/JSON). 마감 시 409
    @PostMapping("/{category:coffee|food}/{team}/orders")
    @ResponseBody
    public ResponseEntity<String> submit(@PathVariable String category, @PathVariable String team,
                                         @RequestBody SubmitRequest body) {
        try {
            orderService.submit(category, team, body.person(), body.lines());
            return ResponseEntity.ok("ok");
        } catch (BoardClosedException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // 주문 제출 요청 바디
    public record SubmitRequest(String person, List<OrderService.LineInput> lines) {}
}
