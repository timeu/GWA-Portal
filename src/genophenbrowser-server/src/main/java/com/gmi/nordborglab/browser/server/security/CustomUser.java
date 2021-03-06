package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.User;
import org.springframework.social.security.SocialUserDetails;

import java.util.Collection;

public class CustomUser extends User implements SocialUserDetails {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private AppUser appUser;

    public CustomUser(AppUser appUser,
                      Collection<? extends GrantedAuthority> authorities) {
        super(appUser.getId().toString(), appUser.getPassword(), appUser.isEnabled(), appUser.isAccountNonExpired(), appUser.isCredentialsNonExpired(),
                appUser.isAccountNonLocked(), authorities);
        this.appUser = appUser;
    }

    public Long getId() {
        return appUser.getId();
    }

    public String getEmail() {
        return appUser.getEmail();
    }

    public String getFirstname() {
        return appUser.getFirstname();
    }

    public String getLastname() {
        return appUser.getLastname();
    }

    public String getJson() {
        return SecurityUtil.serializeUserToJson(this);
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser user) {
        this.appUser = user;
    }

    public String getFullName() {
        return (String.format("%s %s", getFirstname(), getLastname())).trim();
    }

    @Override
    public String getUserId() {
        return getUsername();
    }
}
