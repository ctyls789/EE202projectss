package com.project.core.security;

import com.project.employeeuser.dao.EmployeeUserDAO;
import com.project.employeeuser.model.EmployeeUser;
import com.project.core.dao.RolePermissionRepository;
import com.project.core.dao.UserRoleRepository;
import com.project.core.model.RolePermission;
import com.project.core.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeUserDAO employeeUserDAO;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EmployeeUser employeeUser = employeeUserDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<UserRole> userRoles = userRoleRepository.findByEmployeeId(employeeUser.getEmployeeUserId().intValue());

        List<GrantedAuthority> authorities = userRoles.stream()
                .flatMap(userRole -> {
                    List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(userRole.getRoleId());
                    return rolePermissions.stream()
                            .map(rp -> new SimpleGrantedAuthority(rp.getPermissionCode()));
                })
                .collect(Collectors.toList());

        // Add roles as authorities as well, typically prefixed with "ROLE_"
        userRoles.forEach(userRole -> authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRoleId()))); // Assuming roleId can be mapped to a role name later

        return new EmployeeUserDetails(employeeUser, authorities); // Fix: Pass EmployeeUser instead of Employee
    }
}
