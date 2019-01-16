package io.toast;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SocialUserService {

    private SocialApiServers socialApiServers;

    public User getUserByTypeAndToken(SocialType socialType, String accessToken) {

        SocialApiServer apiServer = socialApiServers.resolveApiServer(socialType);

        SocialUserInfo socialUserInfo = apiServer.getUserInfo(accessToken);

        String name = "";
        Long id = 1L;
        Long socialLoginId = socialUserInfo.getId();
        LoginType loginType = new LoginType(socialType);
        Role role = new Role(RoleType.NORMAL_USER);

        return new User(id, name, socialLoginId, loginType, role);
    }
}
