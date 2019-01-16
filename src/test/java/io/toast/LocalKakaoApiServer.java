package io.toast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/* Test 용 Kakao Api Server */
@Component
@Profile("test-local")
class LocalKakaoApiServer implements SocialApiServer {

    @Value("${social.kakao.id}")
    private Long id;

    @Value("${social.kakao.name}")
    private String name;

    @Value("${social.kakao.profile_pic_url}")
    private String profilePicUrl;

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        return new SocialUserInfo(id, name, profilePicUrl);
    }

    @Override
    public boolean supports(SocialType socialType) {
        return socialType == SocialType.KAKAO;
    }
}
