package io.toast.social.application;

import io.toast.auth.Role;
import io.toast.auth.RoleType;
import io.toast.social.domain.SocialApiServer;
import io.toast.social.domain.SocialType;
import io.toast.social.domain.SocialUserInfo;
import io.toast.user.domain.LoginType;
import io.toast.user.domain.User;
import io.toast.user.domain.UserRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class SocialUserService {

    private SocialApiServers socialApiServers;

    private UserRepository userRepository;

    public SocialUserService(SocialApiServers socialApiServers, UserRepository userRepository) {
        this.socialApiServers = socialApiServers;
        this.userRepository = userRepository;
    }

    @Transactional
    public User getUserByTypeAndToken(SocialType socialType, String accessToken) {
        SocialApiServer apiServer = socialApiServers.resolveApiServer(socialType);

        SocialUserInfo socialUserInfo = apiServer.getUserInfo(accessToken);

        // UserRepository를 이용해서
        // 이미 ID가 있으면, 생성하면 안 되고, 찾아서 반환
        // ID가 없으면, 생성한 후 반환
        Long socialLoginId = socialUserInfo.getId();
        Optional<User> user = userRepository.findByLoginTypeSocialTypeAndSocialLoginId(socialType, socialLoginId);
        return user.orElseGet(() -> {
            String name = socialUserInfo.getName();
            LoginType loginType = new LoginType(socialType);
            Role role = new Role(RoleType.NORMAL_USER);
            User toRegister = new User(null, name, socialLoginId, loginType, role);

            return userRepository.saveAndFlush(toRegister);
        });
    }
}
