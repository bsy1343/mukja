// BoardResetScheduler.java — 매일 자정(KST) 모든 (가게×팀) 보드를 초기화한다
package com.mukja.order;

import com.mukja.config.MukjaProperties;
import com.mukja.menu.MenuService;
import com.mukja.menu.domain.Vendor;
import com.mukja.order.sse.OrderSseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BoardResetScheduler {
    private static final Logger log = LoggerFactory.getLogger(BoardResetScheduler.class);

    private final MenuService menuService;
    private final MukjaProperties props;
    private final OrderRepository orderRepository;
    private final OrderSseService sse;

    public BoardResetScheduler(MenuService menuService, MukjaProperties props,
                               OrderRepository orderRepository, OrderSseService sse) {
        this.menuService = menuService;
        this.props = props;
        this.orderRepository = orderRepository;
        this.sse = sse;
    }

    // 매일 00:00 KST: 모든 가게×팀 보드의 주문·마감을 비운다 (당일 재사용)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void resetAllBoardsAtMidnight() {
        int n = 0;
        for (Vendor v : menuService.data().vendors()) {
            for (MukjaProperties.Team t : props.teams()) {
                orderRepository.reset(v.id(), t.id());
                sse.broadcast(v.id(), t.id()); // 열려있는 화면 즉시 갱신
                n++;
            }
        }
        log.info("자정 초기화: {}개 보드 reset 완료", n);
    }
}
