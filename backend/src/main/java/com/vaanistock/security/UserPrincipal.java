package com.vaanistock.security;

import com.vaanistock.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final Long businessId;
    private final String name;
    private final String email;
    private final String password;
    private final String preferredLanguage;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, Long businessId, String name, String email, String password, String preferredLanguage) {
        this.userId = userId;
        this.businessId = businessId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.preferredLanguage = preferredLanguage;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public static UserPrincipal create(User user, Long businessId) {
        return new UserPrincipal(
                user.getId(),
                businessId,
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPreferredLanguage()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
