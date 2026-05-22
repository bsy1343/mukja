// BoardData.java — 주문판 저장 단위 (마감시각 nullable + 주문 목록)
package dev.sybaek.mukja.order.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record BoardData(OffsetDateTime closeAt, List<OrderEntry> orders) {
    // 빈 주문판
    public static BoardData empty() { return new BoardData(null, List.of()); }
}
