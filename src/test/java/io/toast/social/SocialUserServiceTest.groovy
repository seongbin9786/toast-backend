package io.toast.social

import io.toast.social.application.SocialApiServers
import io.toast.social.domain.SocialType
import io.toast.social.application.SocialUserService
import io.toast.user.domain.User
import org.assertj.core.util.Lists
import spock.lang.Specification

class SocialUserServiceTest extends Specification {

    SocialUserService socialUserService

    LocalFacebookApiServer facebookApiServer

    LocalKakaoApiServer kakaoApiServer

    SocialApiServers socialApiServers

    def setup() {
        facebookApiServer = Spy(LocalFacebookApiServer)
        kakaoApiServer = Spy(LocalKakaoApiServer)

        def socialApiServerList = Lists.newArrayList(facebookApiServer, kakaoApiServer)
        socialApiServers = (SocialApiServers) Spy(SocialApiServers, constructorArgs: [socialApiServerList])

        socialUserService = new SocialUserService(socialApiServers)
    }

    def "getUserByTypeAndToken 은 SocialType 을 지원하는 SocialApiServer 인스턴스를 가져온다"() {
        when:
        socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰)

        then:
        1 * socialApiServers.resolveApiServer(소셜타입)

        where:
        소셜타입             | 엑세스토큰
        SocialType.FB    | ""
        SocialType.KAKAO | ""
    }

    def "getUserByTypeAndToken 은 특정 SocialApiServer 의 getUserInfo 를 호출한다"() {
        given:
        def 특정_SocialApiServer = socialApiServers.resolveApiServer(소셜타입)

        when:
        socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰)

        then:
        1 * 특정_SocialApiServer.getUserInfo(엑세스토큰)

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB | ""
        SocialType.KAKAO | ""
    }

    def "getUserByTypeAndToken 은 User 를 반환한다"() {
        when:
        def user = socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰)

        then:
        assert user != null
        assert user instanceof User

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB | ""
        SocialType.KAKAO | ""
    }
}
