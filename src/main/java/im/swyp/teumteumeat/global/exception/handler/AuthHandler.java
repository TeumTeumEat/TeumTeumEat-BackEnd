package im.swyp.teumteumeat.global.exception.handler;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;

public class AuthHandler extends BaseException {
    public AuthHandler(BaseResponseCode code) {
        super(code);
    }
}