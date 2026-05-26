// TeamRoster.java — 팀별 기대 명단 (팀 id → 멤버 이름 목록). 미주문자 판별 기준
package com.mukja.order.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamRoster(Map<String, List<String>> members) {}
