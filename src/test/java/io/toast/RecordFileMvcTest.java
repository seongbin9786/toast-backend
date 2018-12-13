package io.toast;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(RecordController.class) // 이 Controller만 Bean으로 올림. 명시하지 않으면 모두 올림.
public class RecordFileMvcTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RecordController rc;
	
	@MockBean
	private RecordRepository repo;
	
	@MockBean
	private FileUploadManager manager;

	private static final String URL = "/records";
	
	private String 서버_파일_디렉토리 = "C:/spring_temporary_files/download/";
	private String 클라이언트_파일_디렉토리 = "C:/spring_temporary_files/upload/";
	
	private String[] 가능한_음성파일_확장자_배열 = { ".mp3", ".wav", ".m4a", ".flac", ".au" };

	private String 파일명 = "filename";
	private String 기본_확장자 = ".mp3";
	private String 클라이언트에_저장된_음성파일_전체_디렉토리명 = 클라이언트_파일_디렉토리 + 파일명 + 기본_확장자;
	private String 서버에_저장된_음성파일_전체_디렉토리명 = 서버_파일_디렉토리 + 파일명 + 기본_확장자;

	public static final String AUDIO_PREFIX = "audio/";

	@After
	public void deleteFiles() throws Exception {
		Files.deleteIfExists(Paths.get(클라이언트에_저장된_음성파일_전체_디렉토리명));
		Files.deleteIfExists(Paths.get(서버에_저장된_음성파일_전체_디렉토리명));
	}
	
	@After
	public void setMockedManager() {
		rc.setFileUploadManager(manager); // mock된 manager로 다시 변경
	}
	
	private void 파일생성하기(String content, boolean toUpload) throws Exception {
		파일생성하기(기본_확장자, content, toUpload);
	}
	
	private void 파일생성하기(String suffix, String content, boolean toUpload) throws Exception {
		final String charsetName = "utf-8";
		final String path = (toUpload ? 클라이언트_파일_디렉토리 : 서버_파일_디렉토리) + 파일명 + suffix;
		
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(path), charsetName))) {
			writer.write(content);
		}
	}

	private MockMultipartFile mockMultipart타입으로_Mp3파일_작성() throws Exception {
		파일생성하기("업로드 테스트", true);
		
		return getMultipartFile(기본_확장자);
	}
	
	private MockMultipartFile mockMultipart타입으로_텍스트_파일_작성() throws Exception {
		String txt확장자 = ".txt";
		
		파일생성하기(txt확장자, "업로드 테스트", true);
		
		return getMultipartFile(txt확장자);
	}
	
	private MockMultipartFile mockMultipart타입으로_특정_확장자의_파일_작성(String ext) throws Exception {
		파일생성하기(ext, "업로드 테스트", true);
		
		return getMultipartFile(ext);
	}

	private MockMultipartFile getMultipartFile(String ext) throws FileNotFoundException, IOException {
		File 이미_생성된_파일 = new File(클라이언트_파일_디렉토리 + 파일명 + ext);
		FileInputStream fis = new FileInputStream(이미_생성된_파일);

		return new MockMultipartFile(이미_생성된_파일.getName(), fis);
	}

	@Test
	public void 업로드_가능한_녹음_파일의_종류는_아래와_같다() throws Exception {

		// 업로드 가능
		for (String ext : 가능한_음성파일_확장자_배열) {
			// given 
			MockMultipartFile file = mockMultipart타입으로_특정_확장자의_파일_작성(ext);
			
			// when
			mockMvc.perform(multipart(URL).file(file))
			
			// then
			.andExpect(status().isOk());
		}

		// 업로드 불가능 - 그 외
		// given 
		MockMultipartFile file = mockMultipart타입으로_텍스트_파일_작성();
		
		// when
		String errorMsg = mockMvc.perform(multipart(URL).file(file))
		
		// then - 1 - BAD REQUEST 반환
		.andExpect(status().isBadRequest())
		.andReturn().getResponse().getErrorMessage();
		
		assertThat(errorMsg, is(BadFileUploadedException.MSG));

	}
	
	@Test
	public void 학습테스트_파일_이름으로_음성_파일인지_판단한다() throws Exception {
		for (String ext : 가능한_음성파일_확장자_배열) {
			assertTrue(getContentType(ext).startsWith(AUDIO_PREFIX));
		}
	}

	private String getContentType(String ext) throws IOException {
		return Files.probeContentType(Paths.get("a" + ext));
	}
	
	@Test
	public void FileUploadManager_getFileByRecord는_업로드된_파일의_byte_array를_제공해야_한다() throws Exception {
		// given - 1 - 업로드된 폴더에 파일 생성
		파일생성하기("다운로드 테스트", false);
		
		// given - 2 - Manager 생성
		FileUploadManager manager = new FileUploadManager();
		manager.setDownloadPath(서버_파일_디렉토리);
		
		// when
		byte[] arrayFromManager = manager.getFileByRecord(new Record(서버에_저장된_음성파일_전체_디렉토리명));
		
		// then
		assertNotNull(arrayFromManager);
	}
	
	@Test
	public void 녹음_파일을_업로드하면_다운로드_폴더에_저장해야_한다() throws Exception {
		// given - 1 - 파일 생성
		MockMultipartFile file = mockMultipart타입으로_Mp3파일_작성();
		
		// give - 2 - Manager를 원본 구현체로 삽입
		FileUploadManager originalManager = new FileUploadManager();
		originalManager.setDownloadPath(서버_파일_디렉토리);
		rc.setFileUploadManager(originalManager);
		
		// when
		mockMvc.perform(multipart(URL).file(file));

		// then
		Path p = Paths.get(서버에_저장된_음성파일_전체_디렉토리명);
		assertTrue(Files.exists(p));
	}
	
	
}
