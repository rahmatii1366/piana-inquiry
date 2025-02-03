package ir.piana.dev.inquiry.publisher.security.authorization.manager;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;

import java.util.function.Supplier;

public class PublisherAuthorizationManager implements AuthorizationManager {
    @Override
    public AuthorizationDecision check(Supplier authentication, Object object) {
        return new AuthorizationDecision(true);
    }
}
