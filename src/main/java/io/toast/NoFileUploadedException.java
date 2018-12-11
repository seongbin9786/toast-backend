package io.toast;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = NoFileUploadedException.MESSAGE)
public class NoFileUploadedException extends RuntimeException {

	public static final String MESSAGE = "업로드된 파일이 없습니다.";
	
	private static final long serialVersionUID = 1L;

	public NoFileUploadedException() {
	}

	public NoFileUploadedException(String reason) {
		super(reason);
	}
}
