// AdminController.java — 로그인 + 메뉴 관리
package dev.sybaek.mukja.admin;

import dev.sybaek.mukja.menu.MenuRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final MenuRepository menuRepository;
    public AdminController(MenuRepository menuRepository) { this.menuRepository = menuRepository; }

    // PIN 입력 화면
    @GetMapping("/login")
    public String login() { return "admin/login"; }

    // PIN 제출 → 쿠키 설정
    @PostMapping("/auth")
    public String auth(@RequestParam String pin, HttpServletResponse res) {
        Cookie cookie = new Cookie("admin", pin);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        res.addCookie(cookie);
        return "redirect:/admin";
    }

    // 관리자 메인 (현재 메뉴 표시)
    @GetMapping
    public String index(Model model) {
        model.addAttribute("menu", menuRepository.load());
        return "admin/index";
    }
}
