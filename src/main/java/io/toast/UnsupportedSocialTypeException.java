package io.toast;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 인증 정보입니다")
@AllArgsConstructor
public class UnsupportedSocialTypeException extends RuntimeException {

    private SocialType socialType;

}
