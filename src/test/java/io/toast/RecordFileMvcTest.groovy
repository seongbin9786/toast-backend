package io.toast

import com.fasterxml.jackson.databind.ObjectMapper
import constants.RecordExtensions
import io.toast.config.FileConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.multipart.MultipartFile
import spock.mock.DetachedMockFactory
import template.FileTestTemplate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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

    @Autowired
    RecordRepository recordRepository

    @Autowired
    FileUploadManager fileUploadManager

    def objectMapper = new ObjectMapper()

    static final URL = "/records"

    static final 기본_확장자 = RecordExtensions.가능한_음성파일_확장자_배열[0]
    static final 파일명 = "filename"

    @Override
    def 필요하면_FileConfig_를_덮어쓰기() {}

    @TestConfiguration
    static class MockConfig {

        def factory = new DetachedMockFactory()

        @Bean
        RecordRepository recordRepository() {
            return factory.Mock(RecordRepository)
        }

        @Bean
        FileUploadManager fileUploadManager() {
            return factory.Mock(FileUploadManager)
        }
    }

    def cleanup() {
        cleanUpFiles()
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
        def errorMsg = result.andReturn().response.errorMessage
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

    def "녹음 파일을 업로드하면 녹음 파일 서비스를 호출해야 한다"() {
        given:
        def 클라이언트_개인의_파일_경로 = 클라이언트_파일_디렉토리() + 파일명 + 기본_확장자
        def file = 테스트용_파일_생성하기(클라이언트_개인의_파일_경로)

        when:
        mockMvc.perform(multipart(URL).file(file))

        then:
        1 * fileUploadManager.saveWithFile(_ as MultipartFile)
    }

    def "녹음 파일 다운로드 시 파일 이름 헤더가 필요하다"() {
        given: "업로드된 녹음 파일의 ID를 준비한다"
        def 녹음_파일_ID = 1L
        recordRepository.findById(1L) >> Optional.of(new Record(1L))
        fileUploadManager.getFileByRecord(new Record(1L)) >> { new byte[100] }

        when: "GET /records/{id} 로 녹음 파일을 다운로드한다"
        def request = mockMvc
                .perform(get(URL + "/" + 녹음_파일_ID))

        then: "녹음 파일이 파일명과 함께 제공된다"
        request.andExpect(status().isOk())
        with(request.andReturn().response) {
            assert contentAsByteArray != null
            assert getHeader("Content-Disposition") != null
            assert getHeader("Content-Disposition").contains("filename")
        }
    }
}
