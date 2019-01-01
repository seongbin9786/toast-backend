package io.toast


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecordController)
// 이 Controller만 Bean으로 올림. 명시하지 않으면 모두 올림.
class RecordMvcTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    RecordController rc

    @Autowired
    RecordRepository recordRepository

    @Autowired
    FileUploadManager manager

    static final URL = "/records"

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

    def setup() {
        setMockedManager()
    }

    // mock된 manager로 다시 변경
    def setMockedManager() {
        rc.setFileUploadManager(manager)
    }

    def "녹음 파일을 업로드할 주소는 POST /records 여야 한다"() {
        when:
        def response = mockMvc.perform(multipart(URL)).andReturn().getResponse()
        def statusCode = response.getStatus()
        def errorMsg = response.getErrorMessage()

        then:
        // 405[Method Not Allowed - 엔드포인트에서 해당 Method를 지원하지 않는 경우] 및
        // 404[Not Found - 엔드포인트가 아예 없는 경우]를 반환하면 안 됨
        assert HttpStatus.METHOD_NOT_ALLOWED.value() != statusCode
        assert HttpStatus.NOT_FOUND.value() != statusCode
        assert HttpStatus.BAD_REQUEST.value() == statusCode
        assert NoFileUploadedException.MESSAGE == errorMsg
    }

    def "개별 조회시 FileUploadManager getFileByRecord를 호출한다"() {
        given:
        def returnVal = Optional.of(new Record(1L))
        recordRepository.findById(_ as Long) >> returnVal

        when:
        mockMvc.perform(get(URL + "/" + 1))
                .andExpect(status().isOk())

        then:
        1 * manager.getFileByRecord(_ as Record)
    }

    def "예외테스트 없는 ID로 조회시 NOT FOUND를 반환해야 한다"() {
        given:
        recordRepository.findById(_ as Long) >> Optional.empty()

        when:
        def 없는_ID = 999999L
        def response = mockMvc.perform(get(URL + "/" + 없는_ID))

        then:
        response.andExpect(status().isNotFound())
        def errorMsg = response
                .andReturn().getResponse().getErrorMessage()

        assert errorMsg == NoRecordException.NO_RECORD_MSG
    }

    def "전체_조회시 repo의 findAll을 호출하고 그 반환값을 반환한다"() {
        given:
        recordRepository.findAll() >> null

        when:
        def response = mockMvc.perform(get(URL))

        then:
        1 * recordRepository.findAll()
        response.andExpect(status().isOk())
    }

}
