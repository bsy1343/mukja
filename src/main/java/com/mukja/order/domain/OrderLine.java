// OrderLine.java — 주문 1줄 (메뉴 + 선택옵션 + 금액)
package com.mukja.order.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLine(int itemId, String name, int unitPrice,
                        Map<String, Object> options, String optionText, int lineTotal) {}
