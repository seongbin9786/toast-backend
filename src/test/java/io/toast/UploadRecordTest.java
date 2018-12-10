package io.toast;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
// 이 Controller만 Bean으로 올림. 명시하지 않으면 모두 올림.
@WebMvcTest(RecordController.class)
public class UploadRecordTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RecordController rc;

	private static final String URL = "/records";
	
	private static final String ROOT_PATH = "C:/spring_temporary_files"; // root of the folder
	private static final String FILE_NAME = "/filename.txt";
	private static final String PATH_AND_FILE_NAME = ROOT_PATH + FILE_NAME;
	private static final Path FILE_PATH = Paths.get(PATH_AND_FILE_NAME);
	
	@BeforeClass
	@AfterClass
	public static void onlyOnce() throws Exception {
		Files.deleteIfExists(FILE_PATH);
	}
	
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
	public void 학습테스트_임시_파일을_작성할_수_있다() throws Exception {
		// given = ROOT_PATH 
		
		// when
		writeFile("something new");
		
		// then
		Path p = Paths.get(PATH_AND_FILE_NAME);
		Files.exists(p);
	}

	private void writeFile(String content) throws Exception {
		String charsetName = "utf-8";
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(PATH_AND_FILE_NAME), charsetName))) {
			writer.write(content);
		}
	}

	@Test
	public void 학습테스트_파일을_업로드할_수_있다() throws Exception {
		// given = 파일
		// MockMvc에서는 file이 아니라 MockMultipartFile이어야 함. 그걸로 변환하는 작업이 아래.
		writeFile("업로드 테스트");
		File written = new File(PATH_AND_FILE_NAME);
		FileInputStream fis = new FileInputStream(written);
		MockMultipartFile file = new MockMultipartFile(written.getName(), fis);

		// when
		mockMvc.perform(multipart(URL).file(file))
		
		//then
		.andExpect(status().isOk());
	}

	@Test
	public void 파일을_업로드하면_Record가_반환되어야_한다() throws Exception {
		// given = 파일
		// MockMvc에서는 file이 아니라 MockMultipartFile이어야 함. 그걸로 변환하는 작업이 아래.
		writeFile("업로드 테스트");
		File written = new File(PATH_AND_FILE_NAME);
		FileInputStream fis = new FileInputStream(written);
		MockMultipartFile file = new MockMultipartFile(written.getName(), fis);

		// when & then = 업로드 이후 파일이 업로드되었다고 정상 반응이 와야 함
		// 1. OK
		// 2. 파일의 ID 반환
		String recordJson = mockMvc.perform(multipart(URL).file(file))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		assertNotNull(recordJson);
		assertNotEquals("", recordJson);
	}
	
}
