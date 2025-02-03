package ir.piana.dev.inquiry.publisher.security.error;

import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.boot.utils.errorprocessor.badrequest.AbstractBadRequestException;

public class NotImplementedAuthenticationType extends AbstractBadRequestException {
    public static final String code = "authentication-type.not-implemented";

    protected NotImplementedAuthenticationType() {
        super(code);
    }

    public static ApiException exception = new NotImplementedAuthenticationType();
}
