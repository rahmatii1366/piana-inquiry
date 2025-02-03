package ir.piana.dev.inquiry.publisher.security.authentication.provider;

import ir.piana.dev.inquiry.publisher.security.authentication.credential.BearerTokenCredential;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;

public class PublisherBearerAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenCredential u = (BearerTokenCredential) authentication;

        return new UsernamePasswordAuthenticationToken(
                u.getPrincipal(), u.getCredentials(), new ArrayList<>() {{
            add(new SimpleGrantedAuthority("admin"));
        }});
    }

    @Override
    public boolean supports(Class<?> authentication) {
        if (authentication.isAssignableFrom(BearerTokenCredential.class))
            return true;
        return false;
    }
}
