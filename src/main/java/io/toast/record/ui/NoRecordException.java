package io.toast.record.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = NoRecordException.NO_RECORD_MSG)
public class NoRecordException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public static final String NO_RECORD_MSG = "존재하지 않는 녹음 파일 ID입니다.";

	public NoRecordException() {
	}
	
	public NoRecordException(String reason) {
		super(reason);
	}
}
