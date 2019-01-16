package io.toast.record.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = BadFileUploadedException.MSG)
public class BadFileUploadedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public static final String MSG = "음성 파일 형태가 아닙니다.";

	public BadFileUploadedException() {
	}
	
	public BadFileUploadedException(String reason) {
		super(reason);
	}
}
