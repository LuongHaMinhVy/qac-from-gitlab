package com.ra.base_spring_boot.security.principle;

import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final IAccountRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Account account = userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (AccountRole accountRole : account.getAccountRoles()) {
            Role role = accountRole.getRole();

            authorities.add(new SimpleGrantedAuthority(role.getRoleCode().name()));

            for (RolePermission rp : role.getRolePermissions()) {
                Permission permission = rp.getPermission();
                authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
            }
        }

        return MyUserDetails.builder()
                .account(account)
                .authorities(authorities)
                .build();
    }
}
