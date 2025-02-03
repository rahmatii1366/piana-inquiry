package ir.piana.dev.inquiry.publisher.security.authentication;

import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.dev.inquiry.publisher.security.error.AuthenticationTypeNotSpecified;

import java.util.Arrays;
import java.util.Optional;

public enum AuthenticationType {
    Basic("basic"),
    Bearer("bearer"),
    BearerJWT("bearer-jwt")
    ;

    private String name;

    AuthenticationType(String name) {
        this.name = name;
    }

    public static Optional<AuthenticationType> byName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        Optional<AuthenticationType> any = Arrays.stream(AuthenticationType.values())
                .filter(type -> name.equalsIgnoreCase(type.name))
                .findAny();
        return any;
    }

    public static AuthenticationType byNameOrThrows(String name) {
        if (name == null) {
            ApiException exception = AuthenticationTypeNotSpecified.exception();
            throw exception;
        }
        Optional<AuthenticationType> any = Arrays.stream(AuthenticationType.values())
                .filter(type -> name.equalsIgnoreCase(type.name))
                .findAny();
        return any.orElseThrow(AuthenticationTypeNotSpecified::exception);
    }
}
