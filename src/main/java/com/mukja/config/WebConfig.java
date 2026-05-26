// WebConfig.java — 인터셉터 등록
package com.mukja.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MukjaProperties props;
    public WebConfig(MukjaProperties props) { this.props = props; }

    // /admin/** 경로에 PIN 인터셉터를 적용한다
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminPinInterceptor(props.adminPin()))
                .addPathPatterns("/admin", "/admin/**");
    }
}
