package io.toast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
// 이 Controller만 Bean으로 올림. 명시하지 않으면 모두 올림.
@WebMvcTest(RecordController.class)
public class UploadRecordMvcTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private RecordRepository repo;
	
	private ObjectMapper mapper = new ObjectMapper();

	private static final String URL = "/records";
	
	private String DOWNLOAD_ROOT_PATH = RecordController.DOWNLOAD_ROOT_PATH;
	private String UPLOAD_ROOT_PATH = "C:/spring_temporary_files/upload";

	private String FILE_NAME = "/filename.txt";
	private String UPLOAD_PATH_AND_FILE_NAME = UPLOAD_ROOT_PATH + FILE_NAME;
	private String DOWNLOAD_PATH_AND_FILE_NAME = DOWNLOAD_ROOT_PATH + FILE_NAME;
	private Path UPLOAD_FILE_PATH = Paths.get(UPLOAD_PATH_AND_FILE_NAME);
	private Path DOWNLOAD_FILE_PATH = Paths.get(DOWNLOAD_PATH_AND_FILE_NAME);
	
	@Before
	@After
	public void onlyOnce() throws Exception {
		Files.deleteIfExists(UPLOAD_FILE_PATH);
		Files.deleteIfExists(DOWNLOAD_FILE_PATH);
	}
	
	@Test
	public void 녹음_파일을_업로드할_주소는_POST_records_여야_한다() throws Exception {
		// given=URL

		MockHttpServletResponse response = mockMvc.perform(multipart(URL)).andReturn().getResponse();
		
		// when
		int statusCode = response.getStatus();
		String message = response.getErrorMessage();
		
		// then
		// 405[Method Not Allowed - 엔드포인트에서 해당 Method를 지원하지 않는 경우] 및
		// 404[Not Found - 엔드포인트가 아예 없는 경우]를 반환하면 안 됨
		assertNotEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), statusCode);
		assertNotEquals(HttpStatus.NOT_FOUND.value(), statusCode);
		assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode);
		assertEquals(NoFileUploadedException.MESSAGE, message);
	}

	@Test
	public void 학습테스트_임시_파일을_작성할_수_있다() throws Exception {
		// given = ROOT_PATH 
		
		// when
		writeFile("something new");
		
		// then
		Path p = Paths.get(UPLOAD_PATH_AND_FILE_NAME);
		assertTrue(Files.exists(p));
	}

	/**
	 * 정해진 경로(PATH_AND_FILE_NAME) 상에 String 값으로 내용을 채워 파일을 생성한다.
	 */
	private void writeFile(String content) throws Exception {
		final String charsetName = "utf-8";
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(UPLOAD_PATH_AND_FILE_NAME), charsetName))) {
			writer.write(content);
		}
	}

	@Test
	public void 학습테스트_파일을_업로드할_수_있다() throws Exception {
		// given = 파일
		MockMultipartFile file = writeTempMockMultipart();

		// when
		mockMvc.perform(multipart(URL).file(file))
		
		//then
		.andExpect(status().isOk());
	}

	/**
	 * 임시 파일 생성 후 MockMultipartFile로 변환하여 반환한다.
	 * 
	 * @return MockMultipartFile
	 */
	private MockMultipartFile writeTempMockMultipart() throws Exception {
		writeFile("업로드 테스트");
		
		// MockMvc에서는 file이 아니라 MockMultipartFile이어야 함. 그걸로 변환하는 작업이 아래.
		File written = new File(UPLOAD_PATH_AND_FILE_NAME);
		FileInputStream fis = new FileInputStream(written);
		MockMultipartFile file = new MockMultipartFile(written.getName(), fis);
		
		return file;
	}

	@Test
	public void 녹음_파일을_업로드하면_Record가_반환되어야_한다() throws Exception {
		MockMultipartFile file = writeTempMockMultipart();
		
		// Repository를 mock함
		when(repo.save(Mockito.any(Record.class))).thenReturn(new Record(1L));

		// when & then = 업로드 이후 파일이 업로드되었다고 정상 반응이 와야 함
		String recordJson = mockMvc.perform(multipart(URL).file(file))

		// 1. OK
		.andExpect(status().isOk())

		// 2. 생성된 Record의 Json 반환
		.andReturn().getResponse().getContentAsString();

		// 3. JSON은 empty이면 안 됨
		assertNotEquals("", recordJson);
		
		// 4. Record 타입이어야 함
		Record created = mapper.readValue(recordJson, Record.class);
		assertTrue(created instanceof Record);

		// 5. ID가 부여되어야 함
		assertNotNull(created.getId());
	}

	@Test
	public void 녹음_파일을_업로드하면_다운로드_폴더에_저장해야_한다() throws Exception {
		// given
		MockMultipartFile file = writeTempMockMultipart();
		
		// when
		mockMvc.perform(multipart(URL).file(file));

		// then
		Path p = Paths.get(DOWNLOAD_PATH_AND_FILE_NAME);
		assertTrue(Files.exists(p));
	}
}
