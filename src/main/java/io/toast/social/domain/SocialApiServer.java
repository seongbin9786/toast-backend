package io.toast.social.domain;

/*
    각 Social Type 에 대응하는 Social Api 를 호출하여 회원 정보를 받아온다.
 */
public interface SocialApiServer {

    SocialUserInfo getUserInfo(String accessToken);

    boolean supports(SocialType socialType);
}
