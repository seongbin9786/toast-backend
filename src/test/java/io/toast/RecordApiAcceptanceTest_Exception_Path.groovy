package io.toast

import constants.RecordExtensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import template.FileTestTemplate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
class RecordApiAcceptanceTest_Exception_Path extends FileTestTemplate {

    @Autowired
    MockMvc mockMvc

    static final URL = "/records"

    def cleanup() {
        cleanUpFiles()
    }

    def "POST /records 에 녹음 파일이 아닌 파일을 업로드하면 실패한다"() {
        given: "업로드할 파일을 준비한다"
        def 파일명 = "filename"
        def 파일 = 테스트용_파일_생성하기(클라이언트_파일_디렉토리() + "/" + 파일명 + 불가능한_확장자)

        when: "녹음 파일을 POST /records로 업로드한다"
        def result = mockMvc
                .perform(multipart(URL).file(파일))

        then: "업로드가 실패한다"
        result.andExpect(status().isBadRequest())

        where:
        불가능한_확장자 << RecordExtensions.불가능한_음성파일_확장자_배열
    }

    def "GET /records/{id} 에 없는 ID에 대응하는 녹음 파일의 다운로드는 실패한다"() {
        given: "존재하지 않는 ID를 준비한다"
        def 녹음_파일_ID = 99999L

        when: "GET /records/{id} 로 녹음 파일 다운로드를 시도한다"
        def request = mockMvc
                .perform(get(URL + "/" + 녹음_파일_ID))

        then: "녹음 파일 다운로드가 실패한다"
        request.andExpect(status().isNotFound())

    }

    def "DELETE /records/{id} 에 없는 ID에 대응하는 녹음 파일의 제거는 실패한다"() {
        given: "존재하지 않는 ID를 준비한다"
        def 녹음_파일_ID = 99999L

        when: "DELETE /records/{id} 로 녹음 파일을 제거를 요청한다"
        def request = mockMvc
                .perform(delete(URL + "/" + 녹음_파일_ID))

        then: "녹음 파일의 제거가 실패한다"
        request.andExpect(status().isNotFound())
    }
}
