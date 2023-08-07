package com.example.leaves.config.services;

import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AppUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Autowired
    public AppUserDetailService(UserRepository userRepository, PermissionService permissionService) {
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository
                .findByEmailAndDeletedIsFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " was not found."));
        return mapToUserDetails(userEntity);
    }

    private UserDetails mapToUserDetails(UserEntity userEntity) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        List<String> roleNames = new ArrayList<>();

        for (RoleEntity role : userEntity.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            roleNames.add(role.getName());
        }

        Set<String> permissionNames = permissionService.findAllPermissionNamesByRoleNameIn(roleNames);
        permissionNames
                .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        return new org.springframework.security.core.userdetails.User(
                userEntity.getEmail(),
                userEntity.getPassword(),
                authorities
        );
    }
}
