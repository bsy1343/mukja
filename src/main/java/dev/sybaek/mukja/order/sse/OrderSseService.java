// OrderSseService.java — 보드별 SseEmitter 관리 및 주문 변경 broadcast
package dev.sybaek.mukja.order.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderSseService {
    private static final long TIMEOUT = 30 * 60 * 1000L;
    private final Map<String, List<SseEmitter>> boards = new ConcurrentHashMap<>();

    // 보드(category-team) 구독자를 등록한다
    public SseEmitter subscribe(String category, String team) {
        String key = category + "-" + team;
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        var list = boards.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);
        emitter.onCompletion(() -> list.remove(emitter));
        emitter.onTimeout(() -> list.remove(emitter));
        emitter.onError(e -> list.remove(emitter));
        return emitter;
    }

    // 해당 보드 구독자 전원에게 order-update 이벤트를 보낸다
    public void broadcast(String category, String team) {
        var list = boards.get(category + "-" + team);
        if (list == null) return;
        for (SseEmitter emitter : list) {
            try { emitter.send(SseEmitter.event().name("order-update").data("updated")); }
            catch (IOException e) { list.remove(emitter); }
        }
    }
}
