package io.toast

import constants.RecordExtensions
import io.toast.config.FileConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import template.FileTestTemplate

import java.nio.file.Files
import java.nio.file.Paths

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = RecordController.class)
class RecordFileMvcTest extends FileTestTemplate {

    @Autowired
    FileConfig fileConfig

    @Autowired
    MockMvc mockMvc

    @Autowired
    RecordController recordController

    @MockBean
    RecordRepository repo

    @MockBean
    FileUploadManager manager

    static final URL = "/records"

    static final 기본_확장자 = RecordExtensions.가능한_음성파일_확장자_배열[0]
    static final 파일명 = "filename"

    def cleanup() {
        cleanUpFiles()
    }

    def 원본_FileUploadManager를_주입하기() {
        recordController.setFileUploadManager(new FileUploadManager(fileConfig))
    }

    def "이 파일 종류들은 업로드가 가능하다"() {
        given:
        def 파일_경로 = 클라이언트_파일_디렉토리() + 파일명 + 가능한_확장자
        def file = 테스트용_파일_생성하기(파일_경로)

        when:
        def result = mockMvc
                .perform(multipart(URL).file(file))

        then:
        result.andExpect(status().isOk())

        where:
        가능한_확장자 << RecordExtensions.가능한_음성파일_확장자_배열
    }

    def "그 외 파일들은 업로드가 불가능하다"() {

        given:
        def file = 테스트용_파일_생성하기(클라이언트_파일_디렉토리() + 파일명 + 불가능한_확장자)

        when:
        def result = mockMvc.perform(multipart(URL).file(file))

        then:
        result.andExpect(status().isBadRequest())

        def errorMsg = result.andReturn().getResponse().getErrorMessage()

        assert errorMsg == BadFileUploadedException.MSG

        where:
        불가능한_확장자 << RecordExtensions.불가능한_음성파일_확장자_배열
    }

    def "[학습테스트] 파일 이름으로 음성 파일인지 판단한다"() {
        final String AUDIO_PREFIX = "audio/"

        expect:
        assert getContentType(가능한_확장자).startsWith(AUDIO_PREFIX)

        where:
        가능한_확장자 << RecordExtensions.가능한_음성파일_확장자_배열
    }

    def "FileUploadManager.getFileByRecord는 업로드된 파일의 byte[]를 제공해야 한다"() {
        given:
        def 파일_경로 = 서버_파일_디렉토리() + 파일명 + 기본_확장자
        파일생성하기(파일_경로)
        def originalManager = new FileUploadManager(fileConfig)

        when:
        byte[] arrayFromManager = originalManager.getFileByRecord(new Record(파일_경로))

        then:
        arrayFromManager != null

        // CLEAN UP - mock된 manager로 다시 변경
        recordController.setFileUploadManager(this.manager)
    }

    def "녹음 파일을 업로드하면 다운로드 폴더에 저장해야 한다"() {
        given:
        def 클라이언트_개인의_파일_경로 = 클라이언트_파일_디렉토리() + 파일명 + 기본_확장자
        def file = 테스트용_파일_생성하기(클라이언트_개인의_파일_경로)
        원본_FileUploadManager를_주입하기()

        when:
        mockMvc.perform(multipart(URL).file(file))

        then:
        def 서버_저장소의_파일_경로 = 서버_파일_디렉토리() + 파일명 + 기본_확장자
        def p = Paths.get(서버_저장소의_파일_경로)
        assert Files.exists(p)

        // CLEAN UP - mock된 manager로 다시 변경
        recordController.setFileUploadManager(this.manager)
    }


}
