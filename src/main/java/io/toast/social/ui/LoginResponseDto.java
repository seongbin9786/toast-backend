package io.toast.social.ui;

import io.toast.auth.Authentication;
import io.toast.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private User user;
    private Authentication auth;
}