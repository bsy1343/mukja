// OrderSseServiceTest.java — 보드별 emitter 등록/브로드캐스트 검증
package dev.sybaek.mukja.order.sse;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class OrderSseServiceTest {
    @Test
    void subscribeReturnsEmitterAndBroadcastDoesNotThrow() {
        OrderSseService sse = new OrderSseService();
        SseEmitter emitter = sse.subscribe("coffee", "sa");
        assertNotNull(emitter);
        assertDoesNotThrow(() -> sse.broadcast("coffee", "sa"));     // 구독자에게 전송
        assertDoesNotThrow(() -> sse.broadcast("coffee", "other"));  // 구독자 없어도 안전
    }
}
