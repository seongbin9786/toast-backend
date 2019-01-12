package io.toast;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "등록된 파일을 찾는 데 실패했습니다.")
public class CannotFindRecordFileException extends RuntimeException {
}
