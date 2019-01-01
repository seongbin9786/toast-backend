package learning

import com.fasterxml.jackson.databind.ObjectMapper
import io.toast.Record
import spock.lang.Specification

class ObjectMapperTests extends Specification {

    ObjectMapper objectMapper = new ObjectMapper()

    def "Record의 ID만 있는 Json으로 Record를 생성할 수 있다"() {
        given:
        String json = "{ \"id\": 1 }"

        when:
        Record read = objectMapper.readValue(json, Record)

        then:
        assert read != null
    }
}
