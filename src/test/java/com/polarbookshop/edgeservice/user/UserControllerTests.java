package com.polarbookshop.edgeservice.user;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import org.springframework.context.annotation.Import;
import com.polarbookshop.edgeservice.config.SecurityConfig;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTests {

    @Autowired
    WebTestClient webClient;

    @MockitoBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenNotAuthenticatedThen401() {
        webClient.get().uri("/user")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedThenReturnUser() {
        var expected = new User(
            "jon.snow", "Jon", "Snow", List.of("employee", "customer"));

        webClient
            .mutateWith(mockOidcLogin().idToken(token -> token
                .claim(StandardClaimNames.PREFERRED_USERNAME, expected.username())
                .claim(StandardClaimNames.GIVEN_NAME, expected.firstName())
                .claim(StandardClaimNames.FAMILY_NAME, expected.lastName())
                .claim("roles", expected.roles())))
            .get().uri("/user")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody(User.class)
            .value(actual -> assertThat(actual).isEqualTo(expected));
    }
}
