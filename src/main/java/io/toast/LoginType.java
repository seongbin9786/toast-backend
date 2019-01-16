package io.toast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class LoginType {
    private SocialType socialType;

    @JsonIgnore
    public boolean isSocialAccount() {
        return socialType != null;
    }
}