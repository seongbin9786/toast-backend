package io.toast.user.domain;

import io.toast.social.domain.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginTypeSocialTypeAndSocialLoginId(SocialType socialType, Long socialLoginId);
}
