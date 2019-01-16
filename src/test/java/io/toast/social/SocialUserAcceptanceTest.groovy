package io.toast.social

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import io.toast.social.ui.LoginResponseDto
import io.toast.social.infra.RealFacebookApiServer
import io.toast.social.infra.RealKakaoApiServer
import io.toast.social.infra.SocialApiHttpConnector
import io.toast.social.infra.SocialApiServerNotRespondingException
import io.toast.social.ui.SocialLoginRequestDto
import io.toast.social.domain.SocialType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@TestPropertySource("classpath:/application.yml")
@ActiveProfiles("test")
@Stepwise // 순서대로
class SocialUserAcceptanceTest extends Specification {

    private static String FACEBOOK_ACCESS_TOKEN

    private static String KAKAO_ACCESS_TOKEN

    // @Value를 @Shared로 하면 값이 인식이 안 되고,
    // static으로 하면 주입이 안 된다.
    // @Autowired도, @Value도 setup()에서 static에 할당하려고 하면 안 된다.
    // 그래서, yaml을 직접 읽기로 함
    def setupSpec() {
        Yaml yaml = new Yaml()
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.yml")
        Map<String, Object> defaultProps = (Map<String, Object>) yaml.loadAll(inputStream).iterator().next() // Spring default 문서 영역
        System.out.println(defaultProps)
        DocumentContext json = JsonPath.parse(defaultProps)
        FACEBOOK_ACCESS_TOKEN = json.read('$.social.facebook.access_token')
        KAKAO_ACCESS_TOKEN = json.read('$.social.kakao.access_token')

        System.out.println(FACEBOOK_ACCESS_TOKEN)
        System.out.println(KAKAO_ACCESS_TOKEN)
        assert FACEBOOK_ACCESS_TOKEN != null
        assert KAKAO_ACCESS_TOKEN != null
    }

    @Autowired
    RealFacebookApiServer realFacebookApiServer

    @Autowired
    RealKakaoApiServer realKakaoApiServer

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    static final URL = "/login"

    def "미가입자가 소셜 로그인으로 최초 로그인 시 회원가입을 진행하고 정상적인 로그인 응답을 반환한다"() {
        given:
        def 아직_가입하지_않은_ID의_소셜로그인_요청_DTO = 소셜로그인_요청_DTO_생성하기(소셜타입, 엑세스토큰)

        when: "POST /login 으로 json 전송하여 소셜 로그인을 요청한다"
        def requestJson = objectMapper.writeValueAsString(아직_가입하지_않은_ID의_소셜로그인_요청_DTO)
        def result = mockMvc
                .perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )

        then: "회원가입 및 로그인에 성공한다"
        소셜로그인_정상_응답인지_검증(result)

        where:
        소셜타입             | 엑세스토큰
        SocialType.FB    | FACEBOOK_ACCESS_TOKEN
        SocialType.KAKAO | KAKAO_ACCESS_TOKEN
    }

    def "이미 가입한 유저가 소셜 로그인을 요청하면 성공한다"() {
        given:
        def 이미_가입한_소셜_ID의_소셜로그인_요청_DTO = 소셜로그인_요청_DTO_생성하기(소셜타입, 엑세스토큰)

        when: "POST /login 으로 json 전송하여 소셜 로그인을 요청한다"
        def requestJson = objectMapper.writeValueAsString(이미_가입한_소셜_ID의_소셜로그인_요청_DTO)
        def result = mockMvc
                .perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )

        then: "로그인에 성공한다"
        소셜로그인_정상_응답인지_검증(result)

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB    | FACEBOOK_ACCESS_TOKEN
        SocialType.KAKAO | KAKAO_ACCESS_TOKEN
    }

    def "유효하지 않은 엑세스 토큰이나 소셜 타입으로 소셜 로그인을 요청하면 실패한다"() {
        given:
        def 유효하지_않은_엑세스토큰이나_소셜타입을_가진_소셜로그인_요청_DTO = 소셜로그인_요청_DTO_생성하기(소셜타입, 엑세스토큰)

        when: "POST /login 으로 json 전송하여 소셜 로그인을 요청한다"
        def requestJson = objectMapper.writeValueAsString(유효하지_않은_엑세스토큰이나_소셜타입을_가진_소셜로그인_요청_DTO)
        def result = mockMvc
                .perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )

        then: "로그인에 실패한다"
        result.andExpect(status().isBadRequest())
        result.andExpect(status().reason("잘못된 인증 정보입니다"))

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB | ""
        SocialType.FB | "12345"
        SocialType.FB | null
        SocialType.KAKAO | ""
        SocialType.KAKAO | "12345"
        SocialType.KAKAO | null
    }

    def "소셜로그인 API 서버가 작동하지 않는 시각에 소셜 로그인을 요청하면 실패한다"() {
        given:
        def 소셜_로그인_요청_DTO = 소셜로그인_요청_DTO_생성하기(소셜타입, 엑세스토큰)

        // socialApiHttpConnector 가 실패한 응답을 주도록 Mocking 하기
        SocialApiHttpConnector socialApiHttpConnector = Mock(SocialApiHttpConnector)
        realFacebookApiServer.socialApiHttpConnector = socialApiHttpConnector
        realKakaoApiServer.socialApiHttpConnector = socialApiHttpConnector
        socialApiHttpConnector.getResultJson(_ as String) >> { throw new SocialApiServerNotRespondingException() }
        socialApiHttpConnector.getResultJson(_ as String, _ as String) >> { throw new SocialApiServerNotRespondingException() }

        when: "POST /login 으로 json 전송하여 소셜 로그인을 요청한다"
        def requestJson = objectMapper.writeValueAsString(소셜_로그인_요청_DTO)
        def result = mockMvc
                .perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )

        then: "로그인에 실패한다"
        result.andExpect(status().isBadRequest())
        result.andExpect(status().reason("소셜 로그인 서버가 응답하지 않습니다"))

        where:
        소셜타입 | 엑세스토큰
        SocialType.FB    | FACEBOOK_ACCESS_TOKEN
        SocialType.KAKAO | KAKAO_ACCESS_TOKEN
    }

    private static SocialLoginRequestDto 소셜로그인_요청_DTO_생성하기(def 소셜타입, def 엑세스토큰) {
        def dto = new SocialLoginRequestDto()
        dto.accessToken = 엑세스토큰
        dto.socialLoginType = 소셜타입

        return dto
    }

    /*
        1. 단순히 값이 존재한다를 검증하는 것보다, 예상한 값과 일치하는 것을 검증하는 게 좋을 것 같다.
        2. 소셜 로그인이므로, 미리 소셜 회원의 신상을 단언 조건으로 넣고, 반환되는 값이 일치하는 지 보는 게 좋을 것 같다.
        3. name, id가 기대했던 값이어야 한다는 것
        4. profilePicUrl은 empty나 null만 아니기로
     */
    private void 소셜로그인_정상_응답인지_검증(ResultActions result) {
        result.andExpect(status().isOk())

        def 응답_JSON = result.andReturn().response.contentAsString
        def 로그인_응답_DTO = objectMapper.readValue(응답_JSON, LoginResponseDto)

        assert 로그인_응답_DTO.user.role.normalUser
        assert 로그인_응답_DTO.user.loginType.socialAccount

        with(로그인_응답_DTO) {
            assert user != null
            with(user) {
                assert name != null
                assert id != null
                assert loginType != null
            }
            assert auth != null
            with(auth) {
                assert accessToken != null
                assert refreshToken != null
            }
        }
    }
}
