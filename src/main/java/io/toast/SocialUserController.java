package io.toast;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/login")
public class SocialUserController {

    private SocialUserService socialUserService;

    private AuthService authService;

    @PostMapping
    public LoginResponseDto socialLogin(@RequestBody SocialLoginRequestDto dto) {
        SocialType socialType = dto.getSocialLoginType();
        String accessToken = dto.getAccessToken();

        User user = socialUserService.getUserByTypeAndToken(socialType, accessToken);

        Authentication auth = authService.getAuthenticationByUser(user);

        return new LoginResponseDto(user, auth);
    }
}
