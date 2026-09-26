package com.pdev.rempms.documentservice.config;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RealmAccessConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {

        final Map<String, List<String>> realmAccess = (Map<String, List<String>>) jwt.getClaims().get("realm_access");

        Map<String, List<String>> resourceAccess = (Map<String, List<String>>) jwt.getClaims().get("resource_access");

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (realmAccess != null) {

            grantedAuthorities = realmAccess.get("roles")
                    .stream()
                    .map(roleName -> "ROLE_" + roleName) // prefix required by Spring Security for roles.
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

        }

        if (resourceAccess != null && (resourceAccess.containsKey("pixel-hire"))) {

            resourceAccess = (Map<String, List<String>>) resourceAccess.get("pixel-hire");

            List<SimpleGrantedAuthority> resourceAccessGrantedAuthority = resourceAccess.get("roles")
                    .stream()
                    .map(roleName -> "ROLE_" + roleName) // prefix required by Spring Security for roles.
                    .map(SimpleGrantedAuthority::new).toList();

            grantedAuthorities.addAll(resourceAccessGrantedAuthority);

        }

        return grantedAuthorities;

    }

}
