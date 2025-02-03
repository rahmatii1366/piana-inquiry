package ir.piana.dev.inquiry.publisher.security.error;

import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.boot.utils.errorprocessor.badrequest.AbstractBadRequestException;

public class BearerHeaderNotSet extends AbstractBadRequestException {
    public static final String code = "bearer-header.not-set";

    protected BearerHeaderNotSet() {
        super(code);
    }

    public static ApiException exception = new BearerHeaderNotSet();
}
