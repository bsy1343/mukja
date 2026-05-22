// OrderController.java — 네비게이션/주문/상태 라우트
package dev.sybaek.mukja.order;

import dev.sybaek.mukja.config.MukjaProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderController {
    private final MukjaProperties props;

    public OrderController(MukjaProperties props) { this.props = props; }

    // 카테고리 선택 화면
    @GetMapping("/")
    public String category() { return "order/category"; }

    // 팀 선택 화면 — 정규식으로 알려진 카테고리만 매핑 (static 경로 보호)
    @GetMapping("/{category:coffee|food}")
    public String team(@PathVariable String category, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("categoryName", "커피");
        model.addAttribute("teams", props.teams());
        return "order/team";
    }
}
