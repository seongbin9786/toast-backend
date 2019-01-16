package io.toast;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public Authentication getAuthenticationByUser(User user) {
        String accessToken = "";
        String refreshToken = "";
        return new Authentication(accessToken, refreshToken);
    }
}
