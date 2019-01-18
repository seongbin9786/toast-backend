package io.toast.social;

import com.jayway.jsonpath.DocumentContext;
import io.toast.social.domain.SocialApiServer;
import io.toast.social.domain.SocialType;
import io.toast.social.domain.SocialUserInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import utils.SocialConfigReader;

/* Test 용 Kakao Api Server */
@Component
@Profile("test-local")
class LocalKakaoApiServer implements SocialApiServer {

    private static String KAKAO_SOCIAL_USER_NAME;
    private static String KAKAO_SOCIAL_PROFILE_PIC_URL;
    private static Long KAKAO_SOCIAL_USER_ID;

    public LocalKakaoApiServer() {
        DocumentContext json = SocialConfigReader.getSocialConfigAsJson();
        KAKAO_SOCIAL_PROFILE_PIC_URL = json.read("$.social.kakao.profile_pic_url");
        KAKAO_SOCIAL_USER_NAME = json.read("$.social.kakao.name");
        KAKAO_SOCIAL_USER_ID = json.read("$.social.kakao.id");
    }

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        return new SocialUserInfo(KAKAO_SOCIAL_USER_ID, KAKAO_SOCIAL_USER_NAME, KAKAO_SOCIAL_PROFILE_PIC_URL);
    }

    @Override
    public boolean supports(SocialType socialType) {
        return socialType == SocialType.KAKAO;
    }
}
