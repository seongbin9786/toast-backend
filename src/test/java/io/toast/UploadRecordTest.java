package io.toast;

import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest
public class UploadRecordTest {

	@Autowired
	private MockMvc mockMvc;
	
	String URL = "/records";
	
	@Test
	public void 녹음_파일을_업로드할_주소는_POST_records_여야_한다() throws Exception {
		// given=URL
		
		// when
		int statusCode = mockMvc.perform(post(URL)).andReturn().getResponse().getStatus();
		
		// then
		// 405[Method Not Allowed] 및
		// 404[Not Found]를 반환하면 안 됨
		assertNotEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), statusCode);
		assertNotEquals(HttpStatus.NOT_FOUND.value(), statusCode);
	}
	
	@Test
	public void 녹음_파일을_업로드할_수_있다() {
		// given
		
		
		// when
		
		
		// then
		
	}

}
