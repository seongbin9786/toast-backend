package io.toast.social.infra;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.toast.social.domain.SocialType;
import io.toast.social.domain.SocialUserInfo;
import io.toast.social.domain.SocialApiServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
    실제 Facebook 소셜 로그인 API에 요청하는 객체
 */
@Component
public class RealFacebookApiServer implements SocialApiServer {

    private SocialApiHttpConnector socialApiHttpConnector;

    /* Spock에서 static 필드로 테스트하다보니 테스트 용이성을 위해 setter 주입으로 사용 */
    @Autowired
    public void setSocialApiHttpConnector(SocialApiHttpConnector socialApiHttpConnector) {
        this.socialApiHttpConnector = socialApiHttpConnector;
    }

    /*
        https://graph.facebook.com/v3.2
        GET /me?fields=id,name,picture
        &access_token=엑세스토큰

        HTTP 1.1 200 OK
        {
          "id": "718245498365327",
          "name": "김성빈",
          "picture": {
            "data": {
              "height": 50,
              "is_silhouette": false, // true여도 사진 링크가 오긴 한다.
              "url": "https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=718245498365327&height=50&width=50&ext=1550025762&hash=AeQZpCLm409ZBKv8",
              "width": 50
            }
          }
        }
         */
    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        String url = "https://graph.facebook.com/v3.2/me?fields=id%2Cname%2Cpicture&access_token=";
        String resultJson = socialApiHttpConnector.getResultJson(url + accessToken);

        DocumentContext json = JsonPath.parse(resultJson);
        Long id = json.read("$.id", Long.class);
        String name = json.read("$.name");
        String profilePicUrl = json.read("$.picture.data.url");

        return new SocialUserInfo(id, name, profilePicUrl);
    }

    @Override
    public boolean supports(SocialType socialType) {
        return socialType == SocialType.FB;
    }
}
