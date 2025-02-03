package ir.piana.dev.inquiry.publisher.security.authentication.filter;

import ir.piana.dev.inquiry.publisher.security.error.BasicHeaderNotSet;
import ir.piana.dev.inquiry.publisher.security.error.BearerHeaderNotSet;
import ir.piana.dev.inquiry.publisher.security.error.ConsideredAuthenticationProviderNotFound;
import ir.piana.dev.inquiry.publisher.security.error.NotImplementedAuthenticationType;
import ir.piana.dev.inquiry.publisher.security.authentication.AuthenticationType;
import ir.piana.dev.inquiry.publisher.security.authentication.credential.BearerTokenCredential;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

public class PublisherAuthenticationFilter extends OncePerRequestFilter {
    private final List<AuthenticationProvider> authenticationProviders;

    public PublisherAuthenticationFilter(List<AuthenticationProvider> authenticationProviders) {
        this.authenticationProviders = authenticationProviders;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (request.getServletPath().equalsIgnoreCase("/api/v1/auth/login"))
            return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authType = request.getHeader("auth-type");
        if (request.getServletPath().startsWith("/oidc-ui/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AuthenticationType> authenticationType = AuthenticationType.byName(authType);
        if (authenticationType.isPresent()) {
            Authentication authenticationInfoProvider = switch (authenticationType.get()) {
                case Basic -> {
                    Optional<String> authorization = Optional.ofNullable(
                            request.getHeader("Authorization"));
                    String basicHeader = authorization.orElseThrow(
                            () -> BasicHeaderNotSet.exception);
                    if (!basicHeader.startsWith("Basic "))
                        throw BasicHeaderNotSet.exception;
                    byte[] decode = Base64.getDecoder().decode(basicHeader.substring(6));
                    String[] basic = new String(decode).split(":");
                    yield new UsernamePasswordAuthenticationToken(basic[0], basic[1]);
                }
                case Bearer -> {
                    Optional<String> authorization = Optional.ofNullable(
                            request.getHeader("Authorization"));
                    String basicHeader = authorization.orElseThrow(
                            () -> BearerHeaderNotSet.exception);
                    if (!basicHeader.startsWith("Bearer "))
                        throw BearerHeaderNotSet.exception;
                    yield new BearerTokenCredential(
                            basicHeader.substring(7));
                }
                case BearerJWT -> throw NotImplementedAuthenticationType.exception;
            };

            Authentication authentication = authenticationProviders.stream().filter(
                            authenticationProvider -> authenticationProvider.supports(authenticationInfoProvider.getClass()))
                    .findFirst().orElseThrow(() -> ConsideredAuthenticationProviderNotFound.exception)
                    .authenticate(authenticationInfoProvider);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        /*SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "a", "a", Arrays.asList(new SimpleGrantedAuthority("admin"))));*/
        filterChain.doFilter(request, response);
    }
}
