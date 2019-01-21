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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
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
import utils.SocialConfigReader

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestPropertySource("classpath:/application.yml")
@ActiveProfiles("test")
@Stepwise // 순서대로
class SocialUserAcceptanceTest extends Specification {

    private static String FACEBOOK_ACCESS_TOKEN
    private static String FACEBOOK_SOCIAL_USER_NAME
    private static Long FACEBOOK_SOCIAL_USER_ID

    private static String KAKAO_ACCESS_TOKEN
    private static String KAKAO_SOCIAL_USER_NAME
    private static Long KAKAO_SOCIAL_USER_ID

    // @Value를 @Shared로 하면 값이 인식이 안 되고,
    // static으로 하면 주입이 안 된다.
    // @Autowired도, @Value도 setup()에서 static에 할당하려고 하면 안 된다.
    // 그래서, yaml을 직접 읽기로 함
    def setupSpec() {
        def json = SocialConfigReader.getSocialConfigAsJson()
        FACEBOOK_ACCESS_TOKEN = json.read('$.social.facebook.access_token')
        FACEBOOK_SOCIAL_USER_NAME = json.read('$.social.facebook.name')
        FACEBOOK_SOCIAL_USER_ID = json.read('$.social.facebook.id')

        KAKAO_ACCESS_TOKEN = json.read('$.social.kakao.access_token')
        KAKAO_SOCIAL_USER_NAME = json.read('$.social.kakao.name')
        KAKAO_SOCIAL_USER_ID = json.read('$.social.kakao.id')
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
        소셜로그인_정상_응답인지_검증(result, 소셜이름, 소셜ID, 유저ID)

        where:
        소셜타입          | 엑세스토큰 | 소셜이름 | 소셜ID | 유저ID
        SocialType.FB    | FACEBOOK_ACCESS_TOKEN | FACEBOOK_SOCIAL_USER_NAME | FACEBOOK_SOCIAL_USER_ID | 1
        SocialType.KAKAO | KAKAO_ACCESS_TOKEN | KAKAO_SOCIAL_USER_NAME | KAKAO_SOCIAL_USER_ID | 2
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
        소셜로그인_정상_응답인지_검증(result, 소셜이름, 소셜ID, 유저ID)

        where:
        소셜타입          | 엑세스토큰 | 소셜이름 | 소셜ID | 유저ID
        SocialType.FB    | FACEBOOK_ACCESS_TOKEN | FACEBOOK_SOCIAL_USER_NAME | FACEBOOK_SOCIAL_USER_ID | 1
        SocialType.KAKAO | KAKAO_ACCESS_TOKEN | KAKAO_SOCIAL_USER_NAME | KAKAO_SOCIAL_USER_ID | 2
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
        socialApiHttpConnector_가_실패한_응답을_주도록_mock_하기()

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

    private void socialApiHttpConnector_가_실패한_응답을_주도록_mock_하기() {
        SocialApiHttpConnector socialApiHttpConnector = Mock(SocialApiHttpConnector)
        realFacebookApiServer.socialApiHttpConnector = socialApiHttpConnector
        realKakaoApiServer.socialApiHttpConnector = socialApiHttpConnector
        socialApiHttpConnector.getResultJson(_ as String) >> { throw new SocialApiServerNotRespondingException() }
        socialApiHttpConnector.getResultJson(_ as String, _ as String) >> {
            throw new SocialApiServerNotRespondingException()
        }
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

        검증만 더 빡세게 하면 소셜 로그인은 끝남.

        로그인 토큰이 여러 개이면 좋겠는데, 불가능한가?
     */
    private void 소셜로그인_정상_응답인지_검증(ResultActions result, String 소셜이름, Long 소셜_ID, Long 유저_ID) {
        result.andExpect(status().isOk())

        def 응답_JSON = result.andReturn().response.contentAsString
        def 로그인_응답_DTO = objectMapper.readValue(응답_JSON, LoginResponseDto)

        assert 로그인_응답_DTO.user.role.normalUser
        assert 로그인_응답_DTO.user.loginType.socialAccount

        with(로그인_응답_DTO) {
            assert user != null
            with(user) {
                assert name == 소셜이름 // 소셜 이름여야함
                assert id == 유저_ID // ID도 사람마다 생성되어야 함
                assert socialLoginId == 소셜_ID // 소셜 아이디여야함
                assert loginType.socialAccount // 소셜 계정여야함
            }
            assert auth != null
            // 장기적으로는 access, refresh 토큰을 empty string 이 아닌걸 반환해야 함
            with(auth) {
                assert accessToken != null
                assert refreshToken != null
            }
        }
    }
}
