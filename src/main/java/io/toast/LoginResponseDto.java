package io.toast;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private User user;
    private Authentication auth;
}