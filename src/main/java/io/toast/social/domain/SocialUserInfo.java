package io.toast.social.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {
    private Long id;
    private String name;
    private String profilePicUrl;
}
