package io.toast.social

import io.toast.social.application.SocialApiServers
import io.toast.social.application.SocialUserService
import io.toast.social.domain.SocialType
import io.toast.social.domain.SocialUserInfo
import io.toast.user.domain.User
import io.toast.user.domain.UserRepository
import org.assertj.core.util.Lists
import spock.lang.Specification

class SocialUserServiceTest extends Specification {

    SocialUserService socialUserService

    LocalFacebookApiServer facebookApiServer

    LocalKakaoApiServer kakaoApiServer

    SocialApiServers socialApiServers

    UserRepository userRepository

    def setup() {
        facebookApiServer = Spy(LocalFacebookApiServer)
        kakaoApiServer = Spy(LocalKakaoApiServer)
        userRepository = Mock(UserRepository)

        // Creating User Service
        def socialApiServerList = Lists.newArrayList(facebookApiServer, kakaoApiServer)
        socialApiServers = (SocialApiServers) Spy(SocialApiServers, constructorArgs: [socialApiServerList])
        socialUserService = new SocialUserService(socialApiServers, userRepository)
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

    def "getUserByTypeAndToken 은 User 가 가입되어 있지 않으면 UserRepository#saveAndFlush 를 호출하고 그 값을 반환한다"() {
        given:
        socialApiServers.resolveApiServer(null) >> facebookApiServer
        facebookApiServer.getUserInfo(null) >> new SocialUserInfo()
        userRepository.findByLoginTypeSocialTypeAndSocialLoginId(null, null) >> Optional.empty()
        User result = new User()
        userRepository.saveAndFlush(_ as User) >> result

        when:
        def user = socialUserService.getUserByTypeAndToken(null, null)

        then:
        1 * userRepository.saveAndFlush(_ as User)
        assert user == result
    }

    def "getUserByTypeAndToken 은 UserRepository#findBySocialTypeAnAndSocialLoginId 를 호출하고, 그 값을 반환한다"() {
        given:

        def facebookApiServer = Spy(LocalFacebookApiServer)
        def kakaoApiServer = Spy(LocalKakaoApiServer)
        def userRepository = Mock(UserRepository)

        // Creating User Service
        def socialApiServerList = Lists.newArrayList(facebookApiServer, kakaoApiServer)
        def socialApiServers = (SocialApiServers) Spy(SocialApiServers, constructorArgs: [socialApiServerList])
        SocialUserService socialUserService = new SocialUserService(socialApiServers, userRepository)

        socialApiServers.resolveApiServer(소셜타입) >> facebookApiServer
        facebookApiServer.getUserInfo(엑세스토큰) >> new SocialUserInfo(1L, null, null)
        userRepository.findByLoginTypeSocialTypeAndSocialLoginId(_ as SocialType, _ as Long) >> { arguments -> System.out.println(arguments[1]) }

        when:
        socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰)

        then:
        1 * userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 1L)

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB | ""
        SocialType.KAKAO | ""
    }
}
