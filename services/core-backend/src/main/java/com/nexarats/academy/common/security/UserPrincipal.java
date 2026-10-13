package com.nexarats.academy.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * Represents the authenticated enterprise identity and authorization context.
 */
public class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String name;
    private final UUID tenantId;
    private final String tenantSlug;
    private final UUID sessionId;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(
            UUID userId,
            String email,
            String name,
            UUID tenantId,
            String tenantSlug,
            UUID sessionId,
            Collection<String> roles,
            Collection<String> permissions
    ) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.tenantId = tenantId;
        this.tenantSlug = tenantSlug;
        this.sessionId = sessionId;
        this.roles = roles != null ? new HashSet<>(roles) : Set.of();
        this.permissions = permissions != null ? new HashSet<>(permissions) : Set.of();

        Set<SimpleGrantedAuthority> auths = new HashSet<>();
        for (String role : this.roles) {
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            auths.add(new SimpleGrantedAuthority(roleName));
        }
        for (String perm : this.permissions) {
            auths.add(new SimpleGrantedAuthority(perm));
        }
        this.authorities = Collections.unmodifiableSet(auths);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Not stored in principal for security
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
