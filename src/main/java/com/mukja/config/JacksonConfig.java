// JacksonConfig.java — ObjectMapper 커스터마이즈 (JavaTime, KST)
package com.mukja.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    // 시간 타입(OffsetDateTime 등)을 ISO 문자열로 직렬화한다
    @Bean
    Jackson2ObjectMapperBuilderCustomizer timeModule() {
        return builder -> builder.modulesToInstall(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
