package io.toast;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import config.FixturesConfig;
import template.FileTestTemplate;

@RunWith(SpringRunner.class)
@WebMvcTest(RecordController.class)
public class RecordFileMvcTest extends FileTestTemplate {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RecordController recordController;
	
	@MockBean
	private RecordRepository repo;
	
	@MockBean
	private FileUploadManager manager;

	private static final String URL = "/records";
	
	private static final String 서버_파일_디렉토리 = FixturesConfig.FILES_ROOT_PATH + "download/";
	private static final String 클라이언트_파일_디렉토리 = FixturesConfig.FILES_ROOT_PATH + "upload/";
	
	private static final String[] 가능한_음성파일_확장자_배열 = { ".mp3", ".wav", ".m4a", ".flac", ".au" };
	private static final String 기본_확장자 = 가능한_음성파일_확장자_배열[0];
	private static final String 파일명 = "filename";

	private static final String[] 불가능한_음성파일_확장자_배열 = { ".txt" };

	@Before
	public void createDirectory() throws IOException {
		Files.createDirectories(Paths.get(서버_파일_디렉토리));
		Files.createDirectories(Paths.get(클라이언트_파일_디렉토리));
	}
	
	private File 파일생성하기(String 경로, boolean 업로드용) throws Exception {
		File newFile = new File(경로);
		newFile.createNewFile();
		
		return newFile;
	}

	private MockMultipartFile 테스트용_파일_생성하기(String 경로, boolean 업로드용) throws Exception {
		File toConvert = 파일생성하기(경로, 업로드용);
		FileInputStream fis = new FileInputStream(toConvert);
		return new MockMultipartFile(toConvert.getName(), fis);
	}

	private void 원본_FileUploadManager를_주입하기() {
		recordController.setFileUploadManager(new FileUploadManager(서버_파일_디렉토리));
	}	
	
	@Test
	public void 이_파일_종류들은_업로드가_가능하다() throws Exception {
		for (String 가능한_확장자 : 가능한_음성파일_확장자_배열) {
			// GIVEN 
			String 파일_경로 = 클라이언트_파일_디렉토리 + 파일명 + 가능한_확장자;
			MockMultipartFile file = 테스트용_파일_생성하기(파일_경로, true);
			
			// WHEN
			mockMvc.perform(multipart(URL).file(file))
			
			// THEN
			.andExpect(status().isOk());
		}
	}
	
	@Test
	public void 그_외_파일들은_업로드가_불가능하다() throws Exception {
		for (String 불가능한_확장자 : 불가능한_음성파일_확장자_배열) {
			// GIVEN 
			MockMultipartFile file = 테스트용_파일_생성하기(클라이언트_파일_디렉토리 + 파일명 + 불가능한_확장자, true);
			
			// WHEN
			String errorMsg = mockMvc.perform(multipart(URL).file(file))
			
			// THEN
			.andExpect(status().isBadRequest())
			.andReturn().getResponse().getErrorMessage();
			
			assertThat(errorMsg, is(BadFileUploadedException.MSG));
		}

	}
	
	@Test
	public void 학습테스트_파일_이름으로_음성_파일인지_판단한다() throws Exception {
		final String AUDIO_PREFIX = "audio/";

		for (String ext : 가능한_음성파일_확장자_배열) {
			assertTrue(getContentType(ext).startsWith(AUDIO_PREFIX));
		}
	}

	private String getContentType(String ext) throws IOException {
		return Files.probeContentType(Paths.get("a" + ext));
	}
	
	@Test
	public void FileUploadManager_getFileByRecord는_업로드된_파일의_byte_array를_제공해야_한다() throws Exception {
		// GIVEN - 업로드된 폴더에 파일 생성
		String 파일_경로 = 서버_파일_디렉토리 + 파일명 + 기본_확장자;
		파일생성하기(파일_경로, false);
		
		// GIVEN - FileUploadManager 설정
		FileUploadManager originalManager = new FileUploadManager(서버_파일_디렉토리);
		
		// WHEN
		byte[] arrayFromManager = originalManager.getFileByRecord(new Record(파일_경로));
		
		// THEN
		assertNotNull(arrayFromManager);
		
		// CLEAN UP - mock된 manager로 다시 변경
		recordController.setFileUploadManager(this.manager);
	}
	
	@Test
	public void 녹음_파일을_업로드하면_다운로드_폴더에_저장해야_한다() throws Exception {
		// GIVEN - 파일 생성
		String 클라이언트_개인의_파일_경로 = 클라이언트_파일_디렉토리 + 파일명 + 기본_확장자;
		MockMultipartFile file = 테스트용_파일_생성하기(클라이언트_개인의_파일_경로, true);

		// GIVEN
		원본_FileUploadManager를_주입하기();
		
		// WHEN
		mockMvc.perform(multipart(URL).file(file));

		// THEN
		String 서버_저장소의_파일_경로 = 서버_파일_디렉토리 + 파일명 + 기본_확장자;
		Path p = Paths.get(서버_저장소의_파일_경로);
		assertTrue(Files.exists(p));

		// CLEAN UP - mock된 manager로 다시 변경
		recordController.setFileUploadManager(this.manager);
	}
}
