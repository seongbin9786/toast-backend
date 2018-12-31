package io.toast

import io.toast.config.FileConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Unroll
import template.FileTestTemplate

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = RecordController.class)
class RecordFileMvcTest extends FileTestTemplate {

    @Autowired
    private FileConfig fileConfig

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private RecordController recordController

    @MockBean
    private RecordRepository repo

    @MockBean
    private FileUploadManager manager

    private static final String URL = "/records"

    private static final String[] 가능한_음성파일_확장자_배열 = [".mp3", ".wav", ".m4a", ".flac", ".au"]
    private static final String 기본_확장자 = 가능한_음성파일_확장자_배열[0]
    private static final String 파일명 = "filename"

    private static final String[] 불가능한_음성파일_확장자_배열 = [".txt"]

    private String 서버_파일_디렉토리() {
        return fileConfig.getServerFilesRootPath()
    }

    private String 클라이언트_파일_디렉토리() {
        return fileConfig.getClientFilesRootPath()
    }

    def setup() {
        디렉토리_생성하기()
    }

    def cleanup() {
        cleanUpFiles()
    }

    def 디렉토리_생성하기() {
        System.out.println(서버_파일_디렉토리())
        System.out.println(클라이언트_파일_디렉토리())
        Files.createDirectories(Paths.get(서버_파일_디렉토리()))
        Files.createDirectories(Paths.get(클라이언트_파일_디렉토리()))
    }

    private static File 파일생성하기(String 경로, boolean 업로드용) throws Exception {
        File newFile = new File(경로)
        newFile.createNewFile()

        return newFile
    }

    private static MockMultipartFile 테스트용_파일_생성하기(String 경로, boolean 업로드용) throws Exception {
        File toConvert = 파일생성하기(경로, 업로드용)
        FileInputStream fis = new FileInputStream(toConvert)
        return new MockMultipartFile(toConvert.getName(), fis)
    }

    private void 원본_FileUploadManager를_주입하기() {
        recordController.setFileUploadManager(new FileUploadManager(fileConfig))
    }

    def "이 파일 종류들은 업로드가 가능하다"() {
        given:
        def 파일_경로 = 클라이언트_파일_디렉토리() + 파일명 + 가능한_확장자
        def file = 테스트용_파일_생성하기(파일_경로, true)

        when:
        def result = mockMvc
                .perform(multipart(URL).file(file))

        then:
        result.andExpect(status().isOk())

        where:
        가능한_확장자 << 가능한_음성파일_확장자_배열
    }

    def "그 외 파일들은 업로드가 불가능하다"() {

        given:
        def file = 테스트용_파일_생성하기(클라이언트_파일_디렉토리() + 파일명 + 불가능한_확장자, true)

        when:
        def result = mockMvc.perform(multipart(URL).file(file))

        then:
        result.andExpect(status().isBadRequest())

        def errorMsg = result.andReturn().getResponse().getErrorMessage()

        assert errorMsg == BadFileUploadedException.MSG

        where:
        불가능한_확장자 << 불가능한_음성파일_확장자_배열
    }

    def "[학습테스트] 파일 이름으로 음성 파일인지 판단한다"() {
        final String AUDIO_PREFIX = "audio/"

        expect:

        for (String ext : 가능한_음성파일_확장자_배열) {
            assertTrue(getContentType(ext).startsWith(AUDIO_PREFIX))
        }
    }

    private static String getContentType(String ext) throws IOException {
        return Files.probeContentType(Paths.get("a" + ext))
    }

    def "FileUploadManager.getFileByRecord는 업로드된 파일의 byte[]를 제공해야 한다"() {
        // GIVEN - 업로드된 폴더에 파일 생성
        String 파일_경로 = 서버_파일_디렉토리() + 파일명 + 기본_확장자
        파일생성하기(파일_경로, false)

        // GIVEN - FileUploadManager 설정
        FileUploadManager originalManager = new FileUploadManager(fileConfig)

        // WHEN
        byte[] arrayFromManager = originalManager.getFileByRecord(new Record(파일_경로))

        // THEN
        assertNotNull(arrayFromManager)

        // CLEAN UP - mock된 manager로 다시 변경
        recordController.setFileUploadManager(this.manager)
    }

    def "녹음 파일을 업로드하면 다운로드 폴더에 저장해야 한다"() {
        // GIVEN - 파일 생성
        String 클라이언트_개인의_파일_경로 = 클라이언트_파일_디렉토리() + 파일명 + 기본_확장자
        MockMultipartFile file = 테스트용_파일_생성하기(클라이언트_개인의_파일_경로, true)

        // GIVEN
        원본_FileUploadManager를_주입하기()

        // WHEN
        mockMvc.perform(multipart(URL).file(file))

        // THEN
        String 서버_저장소의_파일_경로 = 서버_파일_디렉토리() + 파일명 + 기본_확장자
        Path p = Paths.get(서버_저장소의_파일_경로)
        assertTrue(Files.exists(p))

        // CLEAN UP - mock된 manager로 다시 변경
        recordController.setFileUploadManager(this.manager)
    }


}
