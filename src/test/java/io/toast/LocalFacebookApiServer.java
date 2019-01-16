package io.toast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/* Test 용 Facebook Api Server */
@Component
@Profile("test-local")
class LocalFacebookApiServer implements SocialApiServer {

    @Value("${social.facebook.id}")
    private Long id;

    @Value("${social.facebook.name}")
    private String name;

    @Value("${social.facebook.profile_pic_url}")
    private String profilePicUrl;

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        return new SocialUserInfo(id, name, profilePicUrl);
    }

    @Override
    public boolean supports(SocialType socialType) {
        return socialType == SocialType.FB;
    }
}
