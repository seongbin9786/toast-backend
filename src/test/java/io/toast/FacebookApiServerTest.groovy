package io.toast

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@SpringBootTest(classes = ToastBackendApplication)
@TestPropertySource("classpath:/application.yml")
@ActiveProfiles(inheritProfiles = false, value = [ "test", "test-local"])
class FacebookApiServerTest extends Specification {

    @Value('${social.facebook.id}')
    private Long 아이디

    @Value('${social.facebook.name}')
    private String 이름

    @Value('${social.facebook.access_token}')
    private String 엑세스토큰

    @Autowired
    RealFacebookApiServer realFacebookApiServer

    @Autowired
    LocalFacebookApiServer localFacebookApiServer

    def "[Local] FacebookApiServer 는 SocialType.FB 만을 지원한다"() {
        when:
        def supports = localFacebookApiServer.supports(소셜타입)

        then:
        assert supports == 지원여부

        where:
        소셜타입              | 지원여부
        SocialType.FB     | true
        SocialType.KAKAO  | false
        SocialType.NAVER  | false
        SocialType.GOOGLE | false
    }

    def "[Local] FacebookApiServer#getUserInfo 는 SocialUserInfo 인스턴스를 반환한다"() {
        given: "Spring Yml에서 정보를 제대로 읽었는지 확인한다"
        assert 엑세스토큰 != null
        assert 이름 != null
        assert 아이디 != null

        when:
        def socialUserInfo = localFacebookApiServer.getUserInfo(엑세스토큰)

        then:
        with (socialUserInfo) {
            assert name == 이름
            assert profilePicUrl != null
            assert profilePicUrl != ""
            assert id == 아이디
        }
    }

    def "[Real] FacebookApiServer#getUserInfo 는 SocialUserInfo 인스턴스를 반환한다"() {
        given: "Spring Yml에서 정보를 제대로 읽었는지 확인한다"
        assert 엑세스토큰 != null
        assert 이름 != null
        assert 아이디 != null

        when:
        def socialUserInfo = realFacebookApiServer.getUserInfo(엑세스토큰)

        then:
        with (socialUserInfo) {
            assert name == 이름
            assert profilePicUrl != null
            assert profilePicUrl != ""
            assert id == 아이디
        }
    }
}
