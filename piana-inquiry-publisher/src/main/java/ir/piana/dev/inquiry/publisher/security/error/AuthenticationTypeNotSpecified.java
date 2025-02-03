package ir.piana.dev.inquiry.publisher.security.error;

import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.boot.utils.errorprocessor.badrequest.AbstractBadRequestException;

public class AuthenticationTypeNotSpecified extends AbstractBadRequestException {
    public static final String code = "authentication-type-header.not-specified";

    protected AuthenticationTypeNotSpecified() {
        super(code);
    }

//    public final static ApiException exception = new AuthenticationTypeNotSpecified();

    public static ApiException exception() {
        ApiException authenticationTypeNotSpecified = new AuthenticationTypeNotSpecified();
        return authenticationTypeNotSpecified;
    }
}
