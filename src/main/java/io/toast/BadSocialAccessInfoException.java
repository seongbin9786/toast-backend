package io.toast;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 인증 정보입니다")
public class BadSocialAccessInfoException extends RuntimeException {
}
