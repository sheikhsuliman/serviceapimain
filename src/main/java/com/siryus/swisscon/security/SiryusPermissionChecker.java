package com.siryus.swisscon.security;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPermissionEvaluator;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SiryusPermissionChecker implements PermissionEvaluator {
    public static final Serializable NULL_EQUIVALENT_ID = 0;

    private final PermissionEvaluator delegate;
    private final Map<Class<?>, List<String>> delegatedTargets;

    private PermissionRepository permissionRepository;

    @Autowired
    public void setPermissionRepository(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    private RoleRepository rolesRepository;

    @Autowired
    public void setRolesRepository(RoleRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    private final TargetResolverCache resolverCache;

    private final UserDefaultResolver userDefaultResolver;

    public SiryusPermissionChecker(TargetResolverCache resolverCache, UserDefaultResolver userDefaultResolver) {
        this.resolverCache = resolverCache;
        this.userDefaultResolver = userDefaultResolver;
        this.delegate = new LemonPermissionEvaluator();

        // TODO: This feels clumsy, there should be a better way to design this (HF, 2020-01-21, SIR-756)
        this.delegatedTargets = new HashMap<>();
        this.delegatedTargets.put(User.class, Collections.singletonList("edit"));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object requiredPermission) {
        // TODO: Now, this is definitely very clumsy. (HF, 2020-01-21, SIR-756)
        if (hasToDelegate(target, requiredPermission)) {
            return this.delegate.hasPermission(authentication, target, requiredPermission);
        }
        return hasPermission(
            authentication,
            requiredPermission,
            this.resolverCache.getResolver(target.getClass()).resolveTarget(target)
        );
    }

    @Override
    public boolean hasPermission(
        Authentication authentication, Serializable targetId, String targetType, Object requiredPermission
    ) {
        final AuthorizationTarget authorizationTarget;

        if (NULL_EQUIVALENT_ID == targetId) {
            final Integer userId = this.resolveUserId(authentication);
            authorizationTarget = this.userDefaultResolver.resolveDefaultTarget(userId, targetType);
        } else {
            authorizationTarget = this.resolverCache.getResolver(targetType).resolveTarget(targetId);
        }

        return hasPermission(authentication, requiredPermission, authorizationTarget);
    }

    private boolean hasToDelegate(Object target, Object requiredPermission) {
        return null != this.delegate
                &&
                this.delegatedTargets.containsKey(target.getClass())
                &&
                this.delegatedTargets.get(target.getClass()).contains(requiredPermission.toString());
    }

    private boolean hasPermission(Authentication authentication, Object requiredPermission, AuthorizationTarget context) {
        return this.resolvePermissions(authentication, context)
                .stream().map(Permission::getName).collect(Collectors.toSet())
                .contains(requiredPermission.toString());
    }

    private Set<Permission> resolvePermissions(Authentication authentication, AuthorizationTarget context) {
        Integer userId = resolveUserId(authentication);

        Set<Role> roles = rolesRepository.getUserRoles(userId, context.getCompanyId(), context.getProjectId());

        return resolvePermissions(roles);
    }

    private Set<Permission> resolvePermissions(Set<Role> roles) {
        if (roles.isEmpty()) {
            return new HashSet<>();
        }
        return permissionRepository.getPermissionsByRoleNames(roles.stream().map(Role::getName).collect(Collectors.toSet()));
    }

    private Integer resolveUserId(Authentication authentication) {
        // TODO : more elegant way to do this?
        return Integer.valueOf(((LemonPrincipal)authentication.getPrincipal()).currentUser().getId());
    }

    public interface UserDefaultResolver {
        AuthorizationTarget resolveDefaultTarget(Integer userId, String targetType);
    }

    public interface TargetResolver {
        AuthorizationTarget resolveTarget(Object target);
        AuthorizationTarget resolveTarget(Serializable target);
    }

    public interface TargetResolverCache {
        TargetResolver getResolver(Class<?> targetType);
        TargetResolver getResolver(String targetType);
    }
}
