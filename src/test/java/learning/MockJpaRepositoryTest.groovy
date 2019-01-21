package learning


import io.toast.social.LocalFacebookApiServer
import io.toast.social.application.SocialApiServers
import io.toast.social.application.SocialUserService
import io.toast.social.domain.SocialApiServer
import io.toast.social.domain.SocialType
import io.toast.social.domain.SocialUserInfo
import io.toast.user.domain.User
import io.toast.user.domain.UserRepository
import org.assertj.core.util.Lists
import spock.lang.Specification

import static org.junit.Assert.assertTrue

class MockJpaRepositoryTest extends Specification {

    UserRepository userRepository = Mock(UserRepository)

    def "JpaRepository는 Mock이 된다"() {
        given:
        userRepository.findById(1L) >> Optional.empty()

        expect:
        assert userRepository.findById(1L) == Optional.empty()
    }

    def "[되는 코드] JpaRepository는 Mock이 된다"() {
        given:
        userRepository.findByLoginTypeSocialTypeAndSocialLoginId(SocialType.FB, 1L) >> Optional.of(new User())
        userRepository.findById(1L) >> Optional.empty()

        expect: "호출 count 를 안 세는 경우, Mock 한대로 잘 작동함"
        assert userRepository.findByLoginTypeSocialTypeAndSocialLoginId(SocialType.FB, 1L).get() == new User()
        assert userRepository.findById(1L) == Optional.empty()
    }

    def "[안되는 코드] JpaRepository는 Mock이 된다"() {
        given:
        userRepository.findByLoginTypeSocialTypeAndSocialLoginId(SocialType.FB, 1L) >> Optional.of(new User())
        userRepository.findById(1L) >> Optional.empty()

        when:
        def user1 = userRepository.findByLoginTypeSocialTypeAndSocialLoginId(SocialType.FB, 1L)
        def user2 = userRepository.findById(1L)

        then: "호출 count를 세는 경우, Mock 한 게 작동하지 않고 null 만 반환"
        assert user1 != null // Condition not satisfied: user1 != null (false) throws AssertionException
        assert user2 != null
        1 * userRepository.findByLoginTypeSocialTypeAndSocialLoginId(SocialType.FB, 1L)
        1 * userRepository.findById(1L)
    }

    def "시도1"() {
        given:
        LocalFacebookApiServer facebookApiServer = Spy(LocalFacebookApiServer)
        facebookApiServer.getUserInfo(엑세스토큰) >> new SocialUserInfo(Long.valueOf(1L), null, null)

        UserRepository userRepository = Mock(UserRepository)
        userRepository.findByLoginTypeSocialTypeAndSocialLoginId(_ as SocialType, 1L) >> Optional.of(new User())

        assert SocialType.FB == 소셜타입

        List<SocialApiServer> socialApiServerList = Lists.newArrayList(facebookApiServer)
        SocialApiServers socialApiServers = Spy(SocialApiServers, constructorArgs: [socialApiServerList])
        socialApiServers.resolveApiServer(소셜타입) >> facebookApiServer

        SocialUserService socialUserService = new SocialUserService(socialApiServers, userRepository)

        when:
        def user = socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰)

        then:
        assert user instanceof User
        assert user != null
        //1 * socialApiServers.resolveApiServer(소셜타입)
        //1 * facebookApiServer.getUserInfo(엑세스토큰)
        //1 * userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 엑세스토큰)

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB | ""
    }

    def "시도2"() {
        given:
        SocialType 소셜타입 = SocialType.FB
        Long 소셜로그인_ID = 1L
        UserRepository userRepository = Mock(UserRepository)
        userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 소셜로그인_ID) >> Optional.of(new User())

        when:
        Optional<User> user = userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 소셜로그인_ID)

        then:
        assertTrue(user != null)
        1 * userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 소셜로그인_ID)
    }
}
