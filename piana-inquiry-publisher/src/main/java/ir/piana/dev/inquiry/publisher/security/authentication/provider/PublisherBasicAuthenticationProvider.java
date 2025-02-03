package ir.piana.dev.inquiry.publisher.security.authentication.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;

public class PublisherBasicAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken u = (UsernamePasswordAuthenticationToken) authentication;

        return new UsernamePasswordAuthenticationToken(
                u.getPrincipal(), u.getCredentials(), new ArrayList<>() {{
            add(new SimpleGrantedAuthority("admin"));
        }});
    }

    @Override
    public boolean supports(Class<?> authentication) {
        if (authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class))
            return true;
        return false;
    }
}
