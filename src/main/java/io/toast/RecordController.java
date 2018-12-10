package io.toast;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
public class RecordController {

	@PostMapping
	public String upload() {
		return "1";//return new Record().getId().toString();
	}
	
}
