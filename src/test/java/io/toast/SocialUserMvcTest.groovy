package io.toast

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(SocialUserController)
class SocialUserMvcTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @TestConfiguration
    static class MockConfig {

        def factory = new DetachedMockFactory()

        @Bean
        AuthService authService() {
            return factory.Mock(AuthService)
        }

        @Bean
        SocialUserService socialUserService() {
            return factory.Mock(SocialUserService)
        }
    }

    @Autowired
    AuthService authService

    @Autowired
    SocialUserService socialUserService

    static final URL = "/login"

    def "소셜 로그인을 처리하는 주소는 POST /login 여야 한다"() {
        when:
        def response = mockMvc
                .perform(post(URL))
                .andReturn().response

        def statusCode = response.status

        then:
        // 405[Method Not Allowed - 엔드포인트에서 해당 Method를 지원하지 않는 경우] 및
        // 404[Not Found - 엔드포인트가 아예 없는 경우]를 반환하면 안 됨
        assert HttpStatus.METHOD_NOT_ALLOWED.value() != statusCode
        assert HttpStatus.NOT_FOUND.value() != statusCode
        assert HttpStatus.BAD_REQUEST.value() == statusCode
    }

    def "소셜 로그인 처리 시 SocialUserService getUserByTypeAndToken 을 호출한다"() {
        given:
        def 소셜타입 = SocialType.FB
        def 엑세스토큰 = ""
        def loginDto = new SocialLoginRequestDto(소셜타입, 엑세스토큰)

        when:
        String requestJson = objectMapper.writeValueAsString(loginDto)
        mockMvc.perform(
                post(URL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )

        then:
        1 * socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰)
    }

    def "소셜 로그인 처리 시 AuthService getAuthenticationByUser 를 호출한다"() {
        given:
        def 소셜타입 = SocialType.FB
        def 엑세스토큰 = ""
        def loginDto = new SocialLoginRequestDto(소셜타입, 엑세스토큰)
        socialUserService.getUserByTypeAndToken(_, _) >> new User()

        when:
        String requestJson = objectMapper.writeValueAsString(loginDto)
        mockMvc.perform(
                post(URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestJson)
        )

        then:
        1 * authService.getAuthenticationByUser(_ as User)
    }

    def "소셜 로그인 과정이 끝나면 LoginResponseDto 를 반환한다"() {
        given:
        def 소셜타입 = SocialType.FB
        def 엑세스토큰 = ""
        def loginDto = new SocialLoginRequestDto(소셜타입, 엑세스토큰)
        socialUserService.getUserByTypeAndToken(소셜타입, 엑세스토큰) >> new User()
        authService.getAuthenticationByUser(_ as User) >> new Authentication()

        when:
        String requestJson = objectMapper.writeValueAsString(loginDto)
        def result = mockMvc.perform(
                post(URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestJson)
        )

        then:
        def resultJson = result.andReturn().response.contentAsString
        def loginResponseDto = objectMapper.readValue(resultJson, LoginResponseDto)
        assert loginResponseDto != null
    }
}
