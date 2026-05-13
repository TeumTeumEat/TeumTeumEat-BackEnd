package im.swyp.teumteumeat.global.exception;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseCode responseCode;

    public BaseException(BaseResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}
