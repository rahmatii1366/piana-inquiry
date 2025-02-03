package ir.piana.dev.inquiry.publisher.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.piana.boot.utils.MessageProvider;
import ir.piana.boot.utils.errorprocessor.PianaExceptionHandlingOnFilter;
import ir.piana.dev.inquiry.publisher.security.authentication.filter.PublisherAuthenticationFilter;
import ir.piana.dev.inquiry.publisher.security.authentication.manager.PublisherAuthenticationManager;
import ir.piana.dev.inquiry.publisher.security.authentication.provider.PublisherBasicAuthenticationProvider;
import ir.piana.dev.inquiry.publisher.security.authentication.provider.PublisherBearerAuthenticationProvider;
import ir.piana.dev.inquiry.publisher.security.authorization.manager.PublisherAuthorizationManager;
import ir.piana.dev.openidc.core.filter.OIDCAuthenticationFilter;
import ir.piana.dev.openidc.core.service.TokenManagementService;
import ir.piana.dev.openidc.core.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class PublisherSecurityConfig {

    /*@Bean
    MessageProvider messageProvider(MessageSource messageSource) {
        return new MessageProvider(messageSource);
    }*/

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OIDCAuthenticationFilter oidcAuthenticationFilter(
            AuthService authService,
            TokenManagementService tokenManagementService) {
        return new OIDCAuthenticationFilter(authService, tokenManagementService);
    }

    @Bean
    PublisherBasicAuthenticationProvider basicAuthenticationProvider() {
        return new PublisherBasicAuthenticationProvider();
    }

    @Bean
    PublisherBearerAuthenticationProvider bearerAuthenticationProvider() {
        return new PublisherBearerAuthenticationProvider();
    }

    @Bean
    AuthenticationManager publisherAuthenticationManager(
            PasswordEncoder passwordEncoder
    ) {
        return new PublisherAuthenticationManager(passwordEncoder);
    }

    @Bean
    AuthorizationManager publisherAuthorizationManager() {
        return new PublisherAuthorizationManager();
    }

    @Bean
    OncePerRequestFilter pianaExceptionHandlingOnFilter(
            ObjectMapper objectMapper
    ) {
        return new PianaExceptionHandlingOnFilter(objectMapper);
    }

    @Bean
    OncePerRequestFilter publisherAuthenticationFilter(
            List<AuthenticationProvider> authenticationProviders
    ) {
        return new PublisherAuthenticationFilter(authenticationProviders);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OIDCAuthenticationFilter oidcAuthenticationFilter,
            AuthenticationManager authenticationManager,
            AuthorizationManager publisherAuthorizationManager,
            PublisherAuthenticationFilter publisherAuthenticationFilter,
            PianaExceptionHandlingOnFilter pianaExceptionHandlingOnFilter
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                                    "favicon.ico",
                                    "api/v1/messaging",
                                    "/api/v1/auth/**",
                                    "/v2/api-docs",
                                    "/v3/api-docs",
                                    "/v3/api-docs/**",
                                    "/swagger-resources",
                                    "/swagger-resources/**",
                                    "/configuration/ui",
                                    "/configuration/security",
                                    "/swagger-ui/**",
                                    "/webjars/**",
                                    "/swagger-ui.html",
                                    "/swagger-ui/index.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/ddg/v3/api-docs/swagger-config",
                                    "/oidc-ui/**",
                                    "/api/v1/piana/oidc/**"
                            )
                            .permitAll()


                            /*--------------------- mandate ----------------------*/

                            /*------------------------- monitoring -----------------*/
                            .requestMatchers(GET, "/actuator/**")
                            .permitAll();
                    auth.requestMatchers("api/v1/inquiry/**")
                            .access(publisherAuthorizationManager);
//                            .hasAnyAuthority(MONITORING.name())
                    auth.anyRequest().authenticated()
                    ;

                })
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(AbstractHttpConfigurer::disable);
//                .cors(Customizer.withDefaults());
        http.formLogin(AbstractHttpConfigurer::disable);
        http.authenticationManager(authenticationManager);
        http.addFilterBefore(pianaExceptionHandlingOnFilter, DisableEncodeUrlFilter.class);
        http.addFilterAt(oidcAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//        http.addFilterAt(publisherAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        /*http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAt(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(creditorIPFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(creditorActivationFilter, CreditorIpFilter.class);
        http.addFilterAfter(debtorAuthenticationFilter, CreditorActivationFilter.class);
        http.addFilterAfter(actuatorAuthenticationFilter, DebtorAuthenticationFilter.class);*/

        return http.build();

    }
}
