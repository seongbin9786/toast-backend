package io.toast.social.infra;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "소셜 로그인 서버가 응답하지 않습니다")
public class SocialApiServerNotRespondingException extends RuntimeException {
}
