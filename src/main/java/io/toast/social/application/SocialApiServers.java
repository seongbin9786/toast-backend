package io.toast.social.application;

import io.toast.social.domain.SocialApiServer;
import io.toast.social.domain.SocialType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
public class SocialApiServers {

    private List<SocialApiServer> socialApiServerList;

    public SocialApiServer resolveApiServer(SocialType socialType) {
        for (SocialApiServer apiServer : socialApiServerList) {
            if (apiServer.supports(socialType))
                return apiServer;
        }
        throw new UnsupportedSocialTypeException(socialType);
    }

}
