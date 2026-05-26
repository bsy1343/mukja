// MukjaProperties.java — 앱 설정 바인딩 (mukja.*)
package com.mukja.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties("mukja")
public record MukjaProperties(String dataDir, String adminPin, List<Team> teams) {
    // 팀 정의 (id는 URL 경로에 사용)
    public record Team(String id, String name) {}
}
