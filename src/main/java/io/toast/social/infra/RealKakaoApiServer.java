package io.toast.social.infra;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.toast.social.domain.SocialType;
import io.toast.social.domain.SocialUserInfo;
import io.toast.social.domain.SocialApiServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
    실제 Kakao 소셜 로그인 API에 요청하는 객체
 */
@Component
public class RealKakaoApiServer implements SocialApiServer {

    private SocialApiHttpConnector socialApiHttpConnector;

    /* Spock에서 static 필드로 테스트하다보니 테스트 용이성을 위해 setter 주입으로 사용 */
    @Autowired
    public void setSocialApiHttpConnector(SocialApiHttpConnector socialApiHttpConnector) {
        this.socialApiHttpConnector = socialApiHttpConnector;
    }

    /*
        https://kapi.kakao.com/v2
        GET /user/me
        Authorization: Bearer 엑세스토큰

        HTTP 1.1 200 OK
        {
          "id": 1007871701,
          "properties": {
            "nickname": "김성빈",
            "profile_image": "http://k.kakaocdn.net/dn/bQdprz/btqrUdrGosy/4IKPWEZHTkIMWQPxcuui4K/profile_640x640s.jpg",
            "thumbnail_image": "http://k.kakaocdn.net/dn/bQdprz/btqrUdrGosy/4IKPWEZHTkIMWQPxcuui4K/profile_110x110c.jpg"
          },
          "kakao_account": {
            "has_email": true,
            "has_age_range": false,
            "has_birthday": false,
            "has_gender": false
          }
         }
         */
    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";
        String authorization = "Bearer " + accessToken;
        String resultJson = socialApiHttpConnector.getResultJson(authorization, url);

        DocumentContext json = JsonPath.parse(resultJson);
        Long id = json.read("$.id", Long.class);
        String name = json.read("$.properties.nickname");
        String profilePicUrl = json.read("$.properties.thumbnail_image");

        return new SocialUserInfo(id, name, profilePicUrl);
    }

    @Override
    public boolean supports(SocialType socialType) {
        return socialType == SocialType.KAKAO;
    }
}
