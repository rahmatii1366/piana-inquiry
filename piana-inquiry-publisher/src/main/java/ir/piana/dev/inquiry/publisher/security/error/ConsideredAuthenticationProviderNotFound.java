package ir.piana.dev.inquiry.publisher.security.error;

import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.boot.utils.errorprocessor.badrequest.AbstractBadRequestException;

public class ConsideredAuthenticationProviderNotFound extends AbstractBadRequestException {
    public static final String code = "authentication-provider.considered.not-founded";

    protected ConsideredAuthenticationProviderNotFound() {
        super(code);
    }

    public static ApiException exception = new ConsideredAuthenticationProviderNotFound();
}
