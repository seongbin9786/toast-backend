package io.toast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(RecordController.class) // 이 Controller만 Bean으로 올림. 명시하지 않으면 모두 올림.
public class UploadRecordMvcTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RecordController rc;
	
	@MockBean
	private RecordRepository repo;
	
	@MockBean
	private FileUploadManager manager;
	
	private ObjectMapper mapper = new ObjectMapper();

	private static final String URL = "/records";
	
	private String DOWNLOAD_ROOT_PATH = "C:/spring_temporary_files/download/";
	private String UPLOAD_ROOT_PATH = "C:/spring_temporary_files/upload/";

	private String FILE_NAME = "filename.txt";
	private String UPLOAD_PATH_AND_FILE_NAME = UPLOAD_ROOT_PATH + FILE_NAME;
	private String DOWNLOAD_PATH_AND_FILE_NAME = DOWNLOAD_ROOT_PATH + FILE_NAME;
	private Path UPLOAD_FILE_PATH = Paths.get(UPLOAD_PATH_AND_FILE_NAME);
	private Path DOWNLOAD_FILE_PATH = Paths.get(DOWNLOAD_PATH_AND_FILE_NAME);
	
	@Before
	@After
	public void deleteFiles() throws Exception {
		Files.deleteIfExists(UPLOAD_FILE_PATH);
		Files.deleteIfExists(DOWNLOAD_FILE_PATH);	
	}
	
	@After
	public void setMockedManager() {
		rc.setFileUploadManager(manager); // mock된 manager로 다시 변경
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
		writeFile("something new", true);
		
		// then
		Path p = Paths.get(UPLOAD_PATH_AND_FILE_NAME);
		assertTrue(Files.exists(p));
	}

	/**
	 * 정해진 경로(PATH_AND_FILE_NAME) 상에 String 값으로 내용을 채워 파일을 생성한다.
	 */
	private void writeFile(String content, boolean toUpload) throws Exception {
		final String charsetName = "utf-8";
		final String path = toUpload ? UPLOAD_PATH_AND_FILE_NAME : DOWNLOAD_PATH_AND_FILE_NAME;
		
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(path), charsetName))) {
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
		writeFile("업로드 테스트", true);
		
		// MockMvc에서는 file이 아니라 MockMultipartFile이어야 함. 그걸로 변환하는 작업이 아래.
		File written = new File(UPLOAD_PATH_AND_FILE_NAME);
		FileInputStream fis = new FileInputStream(written);
		MockMultipartFile file = new MockMultipartFile(written.getName(), fis);
		
		return file;
	}

	@Test
	public void 녹음_파일을_업로드하면_Record가_반환되어야_한다() throws Exception {
		// given - 1 - 파일 새엇ㅇ
		MockMultipartFile file = writeTempMockMultipart();
		
		// given - 2 - Repository를 mock함
		when(repo.save(Mockito.any(Record.class))).thenReturn(new Record(1L));
		
		// given - 3 - Manager를 mock함
		when(manager.saveWithFile(Mockito.any(MultipartFile.class))).thenReturn(new Record(1L));
		
		// when & then = 업로드 이후 파일이 업로드되었다고 정상 반응이 와야 함
		String recordJson = mockMvc.perform(multipart(URL).file(file))

		// 1. OK
		.andExpect(status().isOk())
		.andReturn().getResponse().getContentAsString();

		// 2. JSON은 empty이면 안 됨
		assertNotEquals("", recordJson);
		
		// 3. Record 타입이어야 함
		Record created = mapper.readValue(recordJson, Record.class);
		assertTrue(created instanceof Record);

		// 4. ID가 부여되어야 함
		assertNotNull(created.getId());
	}

	@Test
	public void 개별_조회시_FileUploadManager_getFileByRecord를_호출한다() throws Exception {
		// given - repo를 mock함
		when(repo.getOne(Mockito.anyLong())).thenReturn(new Record(1L));

		// when - 개별 조회 시
		mockMvc.perform(get(URL + "/" + 1));
		
		// then - manager#getFileByRecord 1회 호출
		verify(manager, times(1)).getFileByRecord(Mockito.any(Record.class));
	}
	
	@Test
	public void FileUploadManager_getFileByRecord는_업로드된_파일의_byte_array를_제공해야_한다() throws Exception {
		// given - 1 - 업로드된 폴더에 파일 생성
		writeFile("다운로드 테스트", false);
		
		// given - 2 - Manager 생성
		FileUploadManager manager = new FileUploadManager();
		manager.setDownloadPath(DOWNLOAD_ROOT_PATH);
		
		// when
		byte[] arrayFromManager = manager.getFileByRecord(new Record(DOWNLOAD_PATH_AND_FILE_NAME));
		
		// then
		assertNotNull(arrayFromManager);
	}
	
	@Test
	public void 전체_조회시_repo의_findAll을_호출하고_그_반환값을_반환한다() throws Exception {
		// given - 1 - repo를 mock
		List<Record> records = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			records.add(new Record(Long.valueOf(i)));
		
		when(repo.findAll()).thenReturn(records);
		
		// when
		String recordsJson = mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		// then - 1 - repo의 findAll을 호출해야 함
		verify(repo, times(1)).findAll();
		
		// then - 2 - repo가 반환한 값을 그대로 반환하는지 확인
		List<Record> recordsFromJson = Arrays.asList(mapper.readValue(recordsJson, Record[].class));
		assertTrue(CollectionUtils.isEqualCollection(records, recordsFromJson));
	}
	
	@Test
	public void 녹음_파일을_업로드하면_다운로드_폴더에_저장해야_한다() throws Exception {
		// given - 1 - 파일 생성
		MockMultipartFile file = writeTempMockMultipart();
		
		// give - 2 - Manager를 원본 구현체로 삽입
		FileUploadManager originalManager = new FileUploadManager();
		originalManager.setDownloadPath(DOWNLOAD_ROOT_PATH);
		rc.setFileUploadManager(originalManager);
		
		// when
		mockMvc.perform(multipart(URL).file(file));

		// then
		Path p = Paths.get(DOWNLOAD_PATH_AND_FILE_NAME);
		assertTrue(Files.exists(p));
	}
}
