package im.swyp.teumteumeat.global.config;

import im.swyp.teumteumeat.global.security.AppleUtil;
import im.swyp.teumteumeat.global.security.component.CustomOAuth2AuthorizationRequestResolver;
import im.swyp.teumteumeat.global.security.exception.CustomAccessDeniedHandler;
import im.swyp.teumteumeat.global.security.exception.CustomAuthenticationEntryPoint;
import im.swyp.teumteumeat.global.security.filter.JwtAuthenticationFilter;
import im.swyp.teumteumeat.global.security.filter.JwtExceptionFilter;
import im.swyp.teumteumeat.global.security.handler.OAuth2FailureHandler;
import im.swyp.teumteumeat.global.security.handler.OAuth2SuccessHandler;
import im.swyp.teumteumeat.global.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.LinkedMultiValueMap;

import static im.swyp.teumteumeat.global.common.Constants.WHITELIST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtExceptionFilter jwtExceptionFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final OAuth2FailureHandler oauth2FailureHandler;
        private final CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;
        private final AppleUtil appleUtil;
        private final im.swyp.teumteumeat.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .csrf(AbstractHttpConfigurer::disable)
                                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(WHITELIST).permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(configurer -> configurer
                                                .authorizationEndpoint(endpoint -> endpoint
                                                                .authorizationRequestRepository(
                                                                                httpCookieOAuth2AuthorizationRequestRepository)
                                                                .authorizationRequestResolver(
                                                                                customOAuth2AuthorizationRequestResolver))
                                                .tokenEndpoint(endpoint -> endpoint
                                                                .accessTokenResponseClient(accessTokenResponseClient()))
                                                .userInfoEndpoint(config -> config
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler)
                                                .failureHandler(oauth2FailureHandler))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class)
                                .exceptionHandling(configurer -> configurer
                                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                                .accessDeniedHandler(customAccessDeniedHandler));

                return http.build();
        }

        @Bean
        public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
                RestClientAuthorizationCodeTokenResponseClient client = new RestClientAuthorizationCodeTokenResponseClient();
                client.setParametersConverter(authorizationCodeGrantRequest -> {
                        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
                        parameters.add(OAuth2ParameterNames.GRANT_TYPE,
                                        AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
                        parameters.add(OAuth2ParameterNames.CODE, authorizationCodeGrantRequest
                                        .getAuthorizationExchange().getAuthorizationResponse().getCode());
                        String redirectUri = authorizationCodeGrantRequest.getAuthorizationExchange()
                                        .getAuthorizationRequest().getRedirectUri();
                        if (redirectUri != null) {
                                parameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
                        }
                        parameters.add(OAuth2ParameterNames.CLIENT_ID,
                                        authorizationCodeGrantRequest.getClientRegistration().getClientId());

                        String registrationId = authorizationCodeGrantRequest.getClientRegistration()
                                        .getRegistrationId();
                        if ("apple".equalsIgnoreCase(registrationId)) {
                                parameters.set("client_secret", appleUtil.createClientSecret());
                        }
                        return parameters;
                });
                return client;
        }
}