package io.toast

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import javax.sql.DataSource

@SpringBootTest
class ToastBackendApplicationTests extends Specification {

    @Autowired
    DataSource dataSource

    def "JPA를 Depdendency로 놓는 경우 dataSource가 있어야 한다"() {
        assert dataSource != null
    }

}
