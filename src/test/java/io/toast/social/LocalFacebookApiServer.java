package io.toast.social;

import com.jayway.jsonpath.DocumentContext;
import io.toast.social.domain.SocialApiServer;
import io.toast.social.domain.SocialType;
import io.toast.social.domain.SocialUserInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import utils.SocialConfigReader;

/* Test 용 Facebook Api Server */
@Component
@Profile("test-local")
public class LocalFacebookApiServer implements SocialApiServer {

    private static String FACEBOOK_SOCIAL_USER_NAME;
    private static String FACEBOOK_SOCIAL_PROFILE_PIC_URL;
    private static Long FACEBOOK_SOCIAL_USER_ID;

    public LocalFacebookApiServer() {
        DocumentContext json = SocialConfigReader.getSocialConfigAsJson();
        FACEBOOK_SOCIAL_PROFILE_PIC_URL = json.read("$.social.facebook.profile_pic_url");
        FACEBOOK_SOCIAL_USER_NAME = json.read("$.social.facebook.name");
        FACEBOOK_SOCIAL_USER_ID = json.read("$.social.facebook.id", Long.class);
    }

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        return new SocialUserInfo(FACEBOOK_SOCIAL_USER_ID, FACEBOOK_SOCIAL_USER_NAME, FACEBOOK_SOCIAL_PROFILE_PIC_URL);
    }

    @Override
    public boolean supports(SocialType socialType) {
        return socialType == SocialType.FB;
    }
}
