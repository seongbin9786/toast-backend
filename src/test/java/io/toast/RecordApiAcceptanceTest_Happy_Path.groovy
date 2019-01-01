package io.toast

import com.fasterxml.jackson.databind.ObjectMapper
import constants.RecordExtensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Shared
import spock.lang.Stepwise
import template.FileTestTemplate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@Stepwise
// 선언된 순서대로 테스트 수행
class RecordApiAcceptanceTest_Happy_Path extends FileTestTemplate {

    @Autowired
    MockMvc mockMvc

    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    @Shared
    Record record

    @Shared
    List<Record> recordList

    static final URL = "/records"

    @Override
    def 필요하면_FileConfig_를_덮어쓰기() {}

    def "POST /records 로 녹음 파일을 업로드할 수 있다"() {
        given: "업로드할 파일을 준비한다"
        def 파일명 = "filename"
        def 파일 = 테스트용_파일_생성하기(클라이언트_파일_디렉토리() + "/" + 파일명 + 가능한_확장자)

        when: "녹음 파일을 POST /records 로 업로드한다"
        def result = mockMvc
                .perform(multipart(URL).file(파일))

        then: "업로드된 녹음 파일의 정보가 제공된다"
        result.andExpect(status().isOk())
        Body_를_Record_인스턴스로_변환(result)
        assert record != null

        where:
        가능한_확장자 << RecordExtensions.가능한_음성파일_확장자_배열
    }

    def "GET /records/{id} 로 ID에 대응하는 녹음 파일을 다운로드할 수 있다"() {
        given: "업로드된 녹음 파일의 ID를 준비한다"
        def 녹음_파일_ID = record.id

        when: "GET /records/{id} 로 녹음 파일을 다운로드한다"
        def request = mockMvc
                .perform(get(URL + "/" + 녹음_파일_ID))

        then: "녹음 파일이 파일명과 크기와 함께 제공된다"
        request.andExpect(status().isOk())
        with(request.andReturn().response) {
            assert contentAsByteArray != null
            assert getHeader("Content-Type") == "application/octet-stream"
            assert getHeader("Content-Disposition") != null
            assert getHeader("Content-Disposition").contains("filename")
            assert getHeader("Content-Length") != null
        }
    }

    def "GET /records 로 전체 녹음 파일의 목록을 가져올 수 있다"() {
        when: "GET /records 로 전체 녹음 파일에 대한 정보를 요청한다"
        def request = mockMvc
                .perform(get(URL))

        then: "녹음 파일의 목록이 제공된다"
        Body_를_Record_배열의_인스턴스로_변환(request)
        assert recordList != null
        assert recordList.size() > 0
    }

    def "DELETE /records/{id} 로 ID에 대응하는 녹음 파일을 제거할 수 있다"() {
        given: "업로드된 녹음 파일의 ID를 준비한다"
        def 녹음_파일_ID = record.id

        when: "DELETE /records/{id} 로 녹음 파일을 제거를 요청한다"
        def request = mockMvc
                .perform(delete(URL + "/" + 녹음_파일_ID))

        then: "다시 녹음 파일을 요청하는 경우, 제거됨을 확인할 수 있다"
        request.andExpect(status().isOk())
        mockMvc.perform(get(URL + "/" + 녹음_파일_ID)).andExpect(status().isNotFound())

        cleanup:
        cleanUpFiles()
    }

    def Body_를_Record_인스턴스로_변환(ResultActions result) {
        def content = result.andReturn().getResponse().contentAsString

        record = objectMapper.readValue(content, Record)
    }

    def Body_를_Record_배열의_인스턴스로_변환(request) {
        def content = request.andReturn().getResponse().contentAsString

        recordList = Arrays.asList(objectMapper.readValue(content, Record[]))
    }

}
