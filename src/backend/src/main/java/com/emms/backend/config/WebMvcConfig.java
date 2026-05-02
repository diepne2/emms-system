package com.emms.backend.config;

import com.emms.backend.security.CurrentUserResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final long MAX_AGE_SECS = 3600;

    private final CurrentUserResolver currentUserResolver;
    private final String frontendUrl;
    private final String frontendHomeUrl;
    private final boolean enableCors;

    public WebMvcConfig(
            CurrentUserResolver currentUserResolver,
            @Value("${frontend.url:http://localhost:5173}") String frontendUrl,
            @Value("${frontend.home-url:http://localhost:3000}") String frontendHomeUrl,
            @Value("${security.cors.enabled:true}") boolean enableCors
    ) {
        this.currentUserResolver = currentUserResolver;
        this.frontendUrl = frontendUrl;
        this.frontendHomeUrl = frontendHomeUrl;
        this.enableCors = enableCors;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        if (!enableCors) return;

        List<String> origins = new ArrayList<>();

        if (StringUtils.hasText(frontendUrl)) {
            origins.add(frontendUrl.trim());
        }

        if (StringUtils.hasText(frontendHomeUrl)
                && !frontendHomeUrl.trim().equals(frontendUrl.trim())) {
            origins.add(frontendHomeUrl.trim());
        }

        if (origins.isEmpty()) {
            origins.add("http://localhost:5173");
        }

        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);


        registry.addMapping("/uploads/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = java.nio.file.Paths.get("uploads")
            .toAbsolutePath()
            .normalize()
            .toUri()
            .toString();

    System.out.println("UPLOAD PATH = " + uploadPath);

    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath);
    }
}