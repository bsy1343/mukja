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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {
    private final MukjaProperties props;
    private final MenuService menuService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderAggregator aggregator;
    private final dev.sybaek.mukja.order.sse.OrderSseService sse;
    private final TeamRosterRepository rosterRepository;

    public OrderController(MukjaProperties props, MenuService menuService,
                           OrderRepository orderRepository, OrderService orderService,
                           OrderAggregator aggregator, dev.sybaek.mukja.order.sse.OrderSseService sse,
                           TeamRosterRepository rosterRepository) {
        this.props = props;
        this.menuService = menuService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.aggregator = aggregator;
        this.sse = sse;
        this.rosterRepository = rosterRepository;
    }

    // 루트 → 기본 보드로 리다이렉트
    @GetMapping("/")
    public String root() {
        return "redirect:/coffee/" + props.teams().get(0).id();
    }

    // 카테고리 단독 경로 → 해당 카테고리 기본 팀 보드로 리다이렉트
    @GetMapping("/{category:coffee|food}")
    public String categoryRedirect(@PathVariable String category) {
        return "redirect:/" + category + "/" + props.teams().get(0).id();
    }

    // 팀 표시 이름 조회 (없으면 id 그대로)
    private String teamName(String teamId) {
        return props.teams().stream().filter(t -> t.id().equals(teamId))
                .map(MukjaProperties.Team::name).findFirst().orElse(teamId);
    }

    // 주문판 화면 (단일 레이아웃)
    @GetMapping("/{category:coffee|food}/{team}")
    public String board(@PathVariable String category, @PathVariable String team, Model model) {
        var board = orderRepository.read(category, team);
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("teams", props.teams());
        model.addAttribute("topCategories", topCategories(category));
        model.addAttribute("subCategories", menuService.categoriesIn(category));
        model.addAttribute("closeAt", board.closeAt());
        return "order/board";
    }

    // 상단 드로어용 상위 카테고리 (해당 group에 데이터가 있으면 활성)
    private java.util.List<TopCategory> topCategories(String current) {
        return java.util.List.of(
            new TopCategory("coffee", "☕ 커피", !menuService.categoriesIn("coffee").isEmpty(), current.equals("coffee")),
            new TopCategory("food", "🍱 점심", !menuService.categoriesIn("food").isEmpty(), current.equals("food")));
    }

    // 드로어 항목 (id, 표시명, 활성여부, 현재선택여부)
    public record TopCategory(String id, String name, boolean available, boolean current) {}

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

    // 보드 집계 결과를 만든다 (헬퍼)
    private dev.sybaek.mukja.order.domain.Aggregation aggregate(String category, String team) {
        var data = menuService.data();
        String place = data.place() != null ? data.place().name() : "";
        return aggregator.aggregate(place, "커피", teamName(team),
                orderRepository.read(category, team), rosterRepository.membersOf(team));
    }

    // 집계/발주 화면. HTMX 요청이면 panel fragment, 아니면 전체 페이지
    @GetMapping("/{category:coffee|food}/{team}/status")
    public String status(@PathVariable String category, @PathVariable String team,
                         @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                         Model model) {
        model.addAttribute("category", category);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("agg", aggregate(category, team));
        return hxRequest != null ? "order/status :: panel" : "order/status";
    }

    // 복사용 요약 텍스트
    @GetMapping(value = "/{category:coffee|food}/{team}/status/summary.txt", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String summary(@PathVariable String category, @PathVariable String team) {
        return aggregate(category, team).summaryText();
    }

    // SSE 구독 (보드별 실시간 갱신)
    @GetMapping("/{category:coffee|food}/{team}/status/stream")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter stream(
            @PathVariable String category, @PathVariable String team) {
        return sse.subscribe(category, team);
    }

    // 마감 시각 설정/해제 (PIN 없음). time이 "HH:MM"이면 오늘 KST 기준으로 설정, null/빈값이면 해제
    @PostMapping("/{category:coffee|food}/{team}/deadline")
    @ResponseBody
    public String deadline(@PathVariable String category, @PathVariable String team,
                           @RequestBody DeadlineRequest body) {
        if (body.time() == null || body.time().isBlank()) {
            orderRepository.clearDeadline(category, team);
        } else {
            var parts = body.time().split(":");
            var now = java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
            var close = now.withHour(Integer.parseInt(parts[0])).withMinute(Integer.parseInt(parts[1]))
                    .withSecond(0).withNano(0);
            orderRepository.setDeadline(category, team, close);
        }
        sse.broadcast(category, team);
        return "ok";
    }

    // 주문판 초기화 (PIN 없음)
    @PostMapping("/{category:coffee|food}/{team}/reset")
    @ResponseBody
    public String reset(@PathVariable String category, @PathVariable String team) {
        orderRepository.reset(category, team);
        sse.broadcast(category, team);
        return "ok";
    }

    // 마감 설정 요청 바디
    public record DeadlineRequest(String time) {}
}
