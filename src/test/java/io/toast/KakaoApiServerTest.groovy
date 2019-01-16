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
class KakaoApiServerTest extends Specification {

    @Value('${social.kakao.id}')
    private Long 아이디

    @Value('${social.kakao.name}')
    private String 이름

    @Value('${social.kakao.access_token}')
    private String 엑세스토큰

    @Autowired
    LocalKakaoApiServer localKakaoApiServer

    @Autowired
    RealKakaoApiServer realKakaoApiServer

    def "[Local] KakaoApiServer 는 SocialType.KAKAO 만을 지원한다"() {
        when:
        def supports = localKakaoApiServer.supports(소셜타입)

        then:
        assert supports == 지원여부

        where:
        소셜타입 | 지원여부
        SocialType.KAKAO  | true
        SocialType.FB     | false
        SocialType.NAVER  | false
        SocialType.GOOGLE | false
    }

    def "[Local] KakaoApiServer#getUserInfo 는 SocialUserInfo 인스턴스를 반환한다"() {
        given: "Spring Yml에서 정보를 제대로 읽었는지 확인한다"
        assert 엑세스토큰 != null
        assert 이름 != null
        assert 아이디 != null

        when:
        def socialUserInfo = localKakaoApiServer.getUserInfo(엑세스토큰)

        then:
        with (socialUserInfo) {
            assert name == 이름
            assert profilePicUrl != null
            assert profilePicUrl != ""
            assert id == 아이디
        }
    }

    def "[Real] KakaoApiServer#getUserInfo 는 SocialUserInfo 인스턴스를 반환한다"() {
        given: "Spring Yml에서 정보를 제대로 읽었는지 확인한다"
        assert 엑세스토큰 != null
        assert 이름 != null
        assert 아이디 != null

        when:
        def socialUserInfo = realKakaoApiServer.getUserInfo(엑세스토큰)

        then:
        with (socialUserInfo) {
            assert name == 이름
            assert profilePicUrl != null
            assert profilePicUrl != ""
            assert id == 아이디
        }
    }
}
