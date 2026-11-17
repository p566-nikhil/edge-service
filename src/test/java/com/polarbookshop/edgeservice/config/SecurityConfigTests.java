package com.polarbookshop.edgeservice.config;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

@WebFluxTest
@Import(SecurityConfig.class)
class SecurityConfigTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenLogoutWithCsrfThenShouldRedirect() {
        given(clientRegistrationRepository.findByRegistrationId("test"))
            .willReturn(Mono.just(testClientRegistration()));

        webTestClient
            .mutateWith(mockOidcLogin())
            .mutateWith(csrf())
            .post().uri("/logout")
            .exchange()
            .expectStatus().isFound();
    }

    @Test
    void whenLogoutWithoutCsrfThenShouldReturn403() {
        webTestClient
            .mutateWith(mockOidcLogin())
            .post().uri("/logout")
            .exchange()
            .expectStatus().isForbidden();
    }

    private ClientRegistration testClientRegistration() {
    return ClientRegistration.withRegistrationId("test")
        .clientId("test")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .authorizationUri("http://localhost/authorize")
        .tokenUri("http://localhost/token")
        .userInfoUri("http://localhost/userinfo")
        .userNameAttributeName("sub")
        .clientName("test")
        .providerConfigurationMetadata(Map.of(
            "end_session_endpoint", "http://localhost/logout"
        ))
        .build();
}
}