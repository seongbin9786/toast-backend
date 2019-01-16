package io.toast

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class UserJpaTest extends Specification {

    @Autowired
    UserRepository repo

    def "User는 id, name, LoginType, Role을 항상 Null이 아닌 속성으로 갖는다"() {
        given:
        User user = new User(null, name, socialLoginId, loginType, role)

        when:
        repo.save(user)

        then:
        with (user) {
            id != null
            name != null
            loginType != null
            if (loginType.socialAccount)
                socialLoginId != null // socialAccount일때만 null이 아니어야 함
            role != null
        }

        where:
        name | socialLoginId | loginType | role
        "name" | null | new LoginType() | new Role()
        "name" | 1L | new LoginType(SocialType.FB) | new Role()
    }

    def "User를 매번 저장할 때 마다 id가 새로 생성되어야 한다"() {
        given:
        User user1 = Id_없는_User_생성하기()
        User user2 = Id_없는_User_생성하기()

        when:
        repo.save(user1)
        repo.save(user2)

        then:
        assert user1 != user2
    }

    def "Role에 RoleType을 주지 않고 생성할 경우 RoleType으로 NORMAL_USER를 갖는다"() {
        given:
        Role role = new Role()

        expect:
        assert role.normalUser
    }

    def "LoginType으로 Social 계정인지 구분한다"() {
        given:
        LoginType loginType = 로그인타입

        expect:
        assert loginType.socialAccount == 소셜계졍여부

        where:
        소셜계졍여부 | 로그인타입
        true | new LoginType(SocialType.FB)
        false | new LoginType()
    }

    private static User Id_없는_User_생성하기() {
        new User(null, "name", null, new LoginType(), new Role())
    }

}
