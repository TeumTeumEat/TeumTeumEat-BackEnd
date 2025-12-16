package im.swyp.teumteumeat.global.config;

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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static im.swyp.teumteumeat.global.common.Constants.WHITELIST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtExceptionFilter jwtExceptionFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final OAuth2FailureHandler oauth2FailureHandler;

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
}