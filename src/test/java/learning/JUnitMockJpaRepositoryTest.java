package learning;

import io.toast.social.LocalFacebookApiServer;
import io.toast.social.application.SocialApiServers;
import io.toast.social.application.SocialUserService;
import io.toast.social.domain.SocialType;
import io.toast.social.domain.SocialUserInfo;
import io.toast.user.domain.User;
import io.toast.user.domain.UserRepository;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class JUnitMockJpaRepositoryTest {

    private SocialApiServers socialApiServers = mock(SocialApiServers.class);
    private LocalFacebookApiServer facebookApiServer = mock(LocalFacebookApiServer.class);
    private UserRepository userRepository = Mockito.mock(UserRepository.class);

    @Test
    public void getUserByTypeAndToken_은_findByLoginTypeSocialTypeAndSocialLoginId_을_1회_호출한다() {

        SocialType 소셜타입 = SocialType.FB;
        String 엑세스토큰 = "";

        when(userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 1L)).thenReturn(Optional.of(new User()));

        when(socialApiServers.resolveApiServer(소셜타입)).thenReturn(facebookApiServer);

        when(facebookApiServer.getUserInfo(엑세스토큰)).thenReturn(new SocialUserInfo(1L, null, null));

        SocialUserService socialUSvc = new SocialUserService(socialApiServers, userRepository);

        User user = socialUSvc.getUserByTypeAndToken(소셜타입, 엑세스토큰);

        assertNotNull(user);

        verify(userRepository, times(1)).findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 1L);
    }

    @Test
    public void Spock_네이놈() {
        SocialType 소셜타입 = SocialType.FB;
        Long 소셜로그인_ID = 1L;
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 소셜로그인_ID)).thenReturn(Optional.of(new User()));

        Optional<User> user = userRepository.findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 소셜로그인_ID);

        assertNotNull(user);
        Mockito.verify(userRepository, times(1)).findByLoginTypeSocialTypeAndSocialLoginId(소셜타입, 소셜로그인_ID);
    }
}
