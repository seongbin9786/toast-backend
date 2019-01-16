package io.toast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialUserInfo {
    private Long id;
    private String name;
    private String profilePicUrl;
}
