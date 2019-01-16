package io.toast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequestDto {
    private SocialType socialLoginType;
    private String accessToken;
}