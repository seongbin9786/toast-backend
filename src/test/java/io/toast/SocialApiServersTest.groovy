package io.toast

import com.google.common.collect.Lists
import spock.lang.Specification

/*
    SocialApiServers 의 역할이나,
    SocialApiServer 들의 공통적인 역할을 테스트한다
 */
class SocialApiServersTest extends Specification {

    static RealFacebookApiServer realFacebookApiServer
    static RealKakaoApiServer realKakaoApiServer
    static SocialApiServers localSocialApiServers
    static SocialApiServers realSocialApiServers

    def setupSpec() {
        realFacebookApiServer = new RealFacebookApiServer()
        realKakaoApiServer = new RealKakaoApiServer()
        realSocialApiServers = new SocialApiServers(
            Lists.newArrayList(realFacebookApiServer, realKakaoApiServer)
        )

        def localFacebookApiServer = new LocalFacebookApiServer()
        def localKakaoApiServer = new LocalKakaoApiServer()
        localSocialApiServers = new SocialApiServers(
            Lists.newArrayList(localFacebookApiServer, localKakaoApiServer)
        )
    }

    def "SocialApiServers#resolveApiServer 는 SocialType 을 지원하는 SocialApiServer 인스턴스를 반환한다"() {
        when:
        def socialApiServer = socialApiServers.resolveApiServer(소셜타입)

        then:
        assert socialApiServer.supports(소셜타입)

        where:
        소셜타입 | socialApiServers
        SocialType.FB | realSocialApiServers
        SocialType.KAKAO | realSocialApiServers
        SocialType.FB | localSocialApiServers
        SocialType.KAKAO | localSocialApiServers
    }

    def "SocialApiServers#resolveApiServer 는 지원하지 않는 SocialType 을 요청하는 경우 UnsupportedSocialTypeException 을 던진다"() {
        when:
        socialApiServers.resolveApiServer(소셜타입)

        then:
        thrown UnsupportedSocialTypeException

        where:
        소셜타입 | socialApiServers
        SocialType.NAVER | realSocialApiServers
        SocialType.GOOGLE | realSocialApiServers
        SocialType.NAVER | localSocialApiServers
        SocialType.GOOGLE | localSocialApiServers
    }

    def "[Real] SocialApiServer 의 인스턴스들은 대상 서버가 응답하지 않는 경우 SocialApiServerNotRespondingException 을 던진다"() {

        given:
        def 엑세스토큰 = ""
        def 소셜_API_서버 = realSocialApiServers.resolveApiServer(소셜타입)
        SocialApiHttpConnector socialApiHttpConnector = Mock(SocialApiHttpConnector)
        realFacebookApiServer.socialApiHttpConnector = socialApiHttpConnector
        realKakaoApiServer.socialApiHttpConnector = socialApiHttpConnector
        socialApiHttpConnector.getResultJson(_ as String) >> { throw new SocialApiServerNotRespondingException() }
        socialApiHttpConnector.getResultJson(_ as String, _ as String) >> { throw new SocialApiServerNotRespondingException() }

        when:
        소셜_API_서버.getUserInfo(엑세스토큰)

        then:
        thrown SocialApiServerNotRespondingException

        where:
        소셜타입 << [ SocialType.FB, SocialType.KAKAO ]
    }

    def "[Real] SocialApiServer 의 인스턴스들은 대상 서버가 200 OK로 응답하지 않은 경우 BadSocialAccessInfoException 을 던진다"() {
        given:
        def 엑세스토큰 = ""
        def 소셜_API_서버 = realSocialApiServers.resolveApiServer(소셜타입)
        SocialApiHttpConnector socialApiHttpConnector = Mock(SocialApiHttpConnector)
        realFacebookApiServer.socialApiHttpConnector = socialApiHttpConnector
        realKakaoApiServer.socialApiHttpConnector = socialApiHttpConnector
        socialApiHttpConnector.getResultJson(_ as String) >> { throw new BadSocialAccessInfoException() }
        socialApiHttpConnector.getResultJson(_ as String, _ as String) >> { throw new BadSocialAccessInfoException() }

        when:
        소셜_API_서버.getUserInfo(엑세스토큰)

        then:
        thrown BadSocialAccessInfoException

        where:
        소셜타입 << [ SocialType.FB, SocialType.KAKAO ]
    }

}
