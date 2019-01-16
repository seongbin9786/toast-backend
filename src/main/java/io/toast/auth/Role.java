package io.toast.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    // 기본 값이 NORMAL_USER
    private RoleType roleType = RoleType.NORMAL_USER;

    @JsonIgnore
    public boolean isNormalUser() {
        return roleType == RoleType.NORMAL_USER;
    }
}