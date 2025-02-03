package ir.piana.dev.inquiry.publisher.security.error;

import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.boot.utils.errorprocessor.badrequest.AbstractBadRequestException;

public class BasicHeaderNotSet extends AbstractBadRequestException {
    public static final String code = "basic-header.not-set";

    protected BasicHeaderNotSet() {
        super(code);
    }

    public static ApiException exception = new BasicHeaderNotSet();
}
