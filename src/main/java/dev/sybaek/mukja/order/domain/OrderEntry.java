// OrderEntry.java — 한 사람의 주문
package dev.sybaek.mukja.order.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEntry(String person, OffsetDateTime submittedAt, List<OrderLine> lines) {}
