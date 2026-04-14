package com.emms.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@ConditionalOnProperty(name = "enable-sso", havingValue = "true")
public class OAuth2ClientLoginConfig {

    @Value("${api.host}")
    private String publicApiUrl;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {

        String clientId = System.getenv("OAUTH2_CLIENT_ID");
        String clientSecret = System.getenv("OAUTH2_CLIENT_SECRET");

        ClientRegistration registration = CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(publicApiUrl + "/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}