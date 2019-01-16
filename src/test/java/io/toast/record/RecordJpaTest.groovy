package io.toast.record

import io.toast.record.domain.Record
import io.toast.record.domain.RecordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class RecordJpaTest extends Specification {

    @Autowired
    RecordRepository repo

    def "Record를 매번 저장할 때 마다 ID가 새로 생성되어야 한다"() {
        given:
        Record a = new Record()
        Record b = new Record()

        when:
        repo.save(a)
        repo.save(b)

        then:
        assert a != b
    }
}
