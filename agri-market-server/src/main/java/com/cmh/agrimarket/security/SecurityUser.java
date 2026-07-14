package com.cmh.agrimarket.security;

import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {
    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getNickname() {
        return user.getNickname();
    }

    public Role getRole() {
        return user.getRole();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        authorities.addAll(permissionCodes(user.getRole()).stream()
                .map(SimpleGrantedAuthority::new)
                .toList());
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(user.getEnabled());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getEnabled());
    }

    private List<String> permissionCodes(Role role) {
        if (role == Role.ADMIN) {
            return List.of(
                    "PERM_USER_MANAGE",
                    "PERM_CATEGORY_MANAGE",
                    "PERM_ORIGIN_MANAGE",
                    "PERM_PRODUCT_MANAGE_ALL",
                    "PERM_ORDER_MANAGE_ALL",
                    "PERM_STATS_VIEW_ALL"
            );
        }
        if (role == Role.FARMER) {
            return List.of(
                    "PERM_PRODUCT_MANAGE_OWN",
                    "PERM_ORDER_MANAGE_OWN",
                    "PERM_STATS_VIEW_OWN"
            );
        }
        if (role == Role.CONSUMER) {
            return List.of(
                    "PERM_PRODUCT_VIEW",
                    "PERM_CART_MANAGE",
                    "PERM_FAVORITE_MANAGE",
                    "PERM_ORDER_CREATE",
                    "PERM_ADDRESS_MANAGE",
                    "PERM_REVIEW_CREATE"
            );
        }
        return List.of();
    }
}
