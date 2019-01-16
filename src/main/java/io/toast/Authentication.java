package io.toast;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Authentication {
    private String accessToken;
    private String refreshToken;
}