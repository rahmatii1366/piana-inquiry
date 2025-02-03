package ir.piana.boot.inquiry.common.dto;

public record ResponseDto(
        Integer resultCode,
        Object result
) {
    public ResponseDto(Object result) {
        this(0, result);
    }

    public ResponseDto(Integer resultCode) {
        this(resultCode, null);
    }

}
