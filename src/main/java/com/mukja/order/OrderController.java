// OrderController.java — 네비게이션/주문/상태 라우트 (보드 = 가게(vendor)×팀)
package com.mukja.order;

import com.mukja.config.MukjaProperties;
import com.mukja.menu.MenuService;
import com.mukja.menu.domain.Vendor;
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
    private final com.mukja.order.sse.OrderSseService sse;
    private final TeamRosterRepository rosterRepository;

    public OrderController(MukjaProperties props, MenuService menuService,
                           OrderRepository orderRepository, OrderService orderService,
                           OrderAggregator aggregator, com.mukja.order.sse.OrderSseService sse,
                           TeamRosterRepository rosterRepository) {
        this.props = props;
        this.menuService = menuService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.aggregator = aggregator;
        this.sse = sse;
        this.rosterRepository = rosterRepository;
    }

    // 첫 팀 id (기본값)
    private String firstTeam() { return props.teams().get(0).id(); }

    // 그룹의 첫 가게 id (이름 오름차순 기준)
    private String firstVendor(String group) {
        return menuService.vendorsIn(group).stream().findFirst().map(Vendor::id).orElse("none");
    }

    // 팀 표시 이름 (없으면 id 그대로)
    private String teamName(String teamId) {
        return props.teams().stream().filter(t -> t.id().equals(teamId))
                .map(MukjaProperties.Team::name).findFirst().orElse(teamId);
    }

    // 가게 표시 이름 (없으면 id 그대로)
    private String vendorName(String vendorId) {
        return menuService.vendor(vendorId).map(Vendor::name).orElse(vendorId);
    }

    // 가게가 속한 상위 그룹 라벨 (커피/점심)
    private String groupLabel(String vendorId) {
        return menuService.vendor(vendorId).map(v -> "coffee".equals(v.group()) ? "커피" : "점심").orElse("");
    }

    // 수량 단위 (커피=잔, 식당=개)
    private String unitOf(String vendorId) {
        return menuService.vendor(vendorId).map(v -> "coffee".equals(v.group()) ? "잔" : "개").orElse("잔");
    }

    // 루트 → 커피 첫 가게 보드로 리다이렉트
    @GetMapping("/")
    public String root() {
        return "redirect:/coffee/" + firstVendor("coffee") + "/" + firstTeam();
    }

    // 카테고리 단독 경로 → 해당 그룹 첫 가게 보드로 리다이렉트
    @GetMapping("/{category:coffee|food}")
    public String categoryRedirect(@PathVariable String category) {
        return "redirect:/" + category + "/" + firstVendor(category) + "/" + firstTeam();
    }

    // 주문판 화면 (가게 단위)
    @GetMapping("/{category:coffee|food}/{vendor}/{team}")
    public String board(@PathVariable String category, @PathVariable String vendor,
                        @PathVariable String team, Model model) {
        var board = orderRepository.read(vendor, team);
        model.addAttribute("category", category);
        model.addAttribute("vendor", vendor);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("teams", props.teams());
        model.addAttribute("members", rosterRepository.membersOf(team));
        model.addAttribute("drawerGroups", drawerGroups());
        model.addAttribute("subCategories", menuService.categoriesOf(vendor));
        model.addAttribute("placeName", vendorName(vendor));
        model.addAttribute("orderedPersons", board.orders().stream().map(o -> o.person()).toList());
        model.addAttribute("closeAt", board.closeAt());
        return "order/board";
    }

    // 드로어용 상위 그룹 + 그룹별 가게 목록 (이름 오름차순)
    private List<DrawerGroup> drawerGroups() {
        return List.of(
            new DrawerGroup("coffee", "☕ 커피", menuService.vendorsIn("coffee")),
            new DrawerGroup("food", "🍱 점심", menuService.vendorsIn("food")));
    }

    // 드로어 그룹 (id, 표시명, 가게 목록)
    public record DrawerGroup(String id, String name, List<Vendor> vendors) {}

    // 서브카테고리 메뉴 그리드 fragment
    @GetMapping("/{category:coffee|food}/{vendor}/{team}/menu")
    public String menuGrid(@PathVariable String category, @PathVariable String vendor,
                           @PathVariable String team, @RequestParam String cat, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("vendor", vendor);
        model.addAttribute("team", team);
        model.addAttribute("subCategory", menuService.category(cat).orElseThrow());
        return "order/fragments/menu-grid :: grid";
    }

    // 옵션 선택 모달 fragment
    @GetMapping("/{category:coffee|food}/{vendor}/{team}/menu/{itemId}/options")
    public String optionModal(@PathVariable String category, @PathVariable String vendor,
                              @PathVariable String team, @PathVariable int itemId, Model model) {
        var item = menuService.item(itemId).orElseThrow();
        model.addAttribute("category", category);
        model.addAttribute("vendor", vendor);
        model.addAttribute("team", team);
        model.addAttribute("item", item);
        model.addAttribute("optionDefs", menuService.data().optionDefs());
        return "order/fragments/option-modal :: modal";
    }

    // 주문 제출/수정 (HTMX/JSON). 마감 시 409
    @PostMapping("/{category:coffee|food}/{vendor}/{team}/orders")
    @ResponseBody
    public ResponseEntity<String> submit(@PathVariable String category, @PathVariable String vendor,
                                         @PathVariable String team, @RequestBody SubmitRequest body) {
        if (body.person() == null || body.person().isBlank()) {
            return ResponseEntity.badRequest().body("이름을 입력하세요");
        }
        if (body.lines() == null || body.lines().size() != 1) {
            return ResponseEntity.badRequest().body("한 번에 메뉴 1개만 주문할 수 있어요");
        }
        try {
            orderService.submit(vendor, team, body.person().trim(), body.lines());
            return ResponseEntity.ok("ok");
        } catch (BoardClosedException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // 주문 제출 요청 바디
    public record SubmitRequest(String person, List<OrderService.LineInput> lines) {}

    // 보드 집계 결과를 만든다 (헬퍼). 보드 키 = 가게(vendor)
    private com.mukja.order.domain.Aggregation aggregate(String vendor, String team) {
        return aggregator.aggregate(vendorName(vendor), groupLabel(vendor), teamName(team),
                orderRepository.read(vendor, team), rosterRepository.membersOf(team), unitOf(vendor));
    }

    // 집계/발주 화면. HTMX 요청이면 panel fragment, 아니면 전체 페이지
    @GetMapping("/{category:coffee|food}/{vendor}/{team}/status")
    public String status(@PathVariable String category, @PathVariable String vendor, @PathVariable String team,
                         @RequestHeader(value = "HX-Request", required = false) String hxRequest, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("vendor", vendor);
        model.addAttribute("team", team);
        model.addAttribute("teamName", teamName(team));
        model.addAttribute("placeName", vendorName(vendor));
        model.addAttribute("unit", unitOf(vendor));
        model.addAttribute("agg", aggregate(vendor, team));
        return hxRequest != null ? "order/status :: panel" : "order/status";
    }

    // 복사용 요약 텍스트
    @GetMapping(value = "/{category:coffee|food}/{vendor}/{team}/status/summary.txt", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String summary(@PathVariable String category, @PathVariable String vendor, @PathVariable String team) {
        return aggregate(vendor, team).summaryText();
    }

    // SSE 구독 (보드별 실시간 갱신)
    @GetMapping("/{category:coffee|food}/{vendor}/{team}/status/stream")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter stream(
            @PathVariable String category, @PathVariable String vendor, @PathVariable String team) {
        return sse.subscribe(vendor, team);
    }

    // 마감 시각 설정/해제 (PIN 없음). time이 "HH:MM"이면 오늘 KST 기준으로 설정, null/빈값이면 해제
    @PostMapping("/{category:coffee|food}/{vendor}/{team}/deadline")
    @ResponseBody
    public ResponseEntity<String> deadline(@PathVariable String category, @PathVariable String vendor,
                           @PathVariable String team, @RequestBody DeadlineRequest body) {
        String time = body.time();
        if (time == null || time.isBlank()) {
            orderRepository.clearDeadline(vendor, team);
        } else if (time.matches("([01]?\\d|2[0-3]):[0-5]\\d")) {
            var parts = time.split(":");
            var now = java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
            var close = now.withHour(Integer.parseInt(parts[0])).withMinute(Integer.parseInt(parts[1]))
                    .withSecond(0).withNano(0);
            orderRepository.setDeadline(vendor, team, close);
        } else {
            return ResponseEntity.badRequest().body("시각 형식은 HH:MM (예: 14:30) 입니다");
        }
        sse.broadcast(vendor, team);
        return ResponseEntity.ok("ok");
    }

    // 주문판 초기화 (PIN 없음)
    @PostMapping("/{category:coffee|food}/{vendor}/{team}/reset")
    @ResponseBody
    public String reset(@PathVariable String category, @PathVariable String vendor, @PathVariable String team) {
        orderRepository.reset(vendor, team);
        sse.broadcast(vendor, team);
        return "ok";
    }

    // 특정 person 주문 취소(삭제)
    @PostMapping("/{category:coffee|food}/{vendor}/{team}/orders/delete")
    @ResponseBody
    public String deleteOrder(@PathVariable String category, @PathVariable String vendor,
                              @PathVariable String team, @RequestBody DeleteRequest body) {
        orderRepository.removePerson(vendor, team, body.person());
        sse.broadcast(vendor, team);
        return "ok";
    }

    // 주문 취소 요청 바디
    public record DeleteRequest(String person) {}

    // 마감 설정 요청 바디
    public record DeadlineRequest(String time) {}
}
