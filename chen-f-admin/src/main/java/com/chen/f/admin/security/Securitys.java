package com.chen.f.admin.security;

import com.chen.f.common.pojo.SysPermission;
import com.chen.f.common.pojo.SysUserRolePermission;
import com.chen.f.core.api.ApiAssert;
import com.chen.f.core.api.response.error.security.SecurityErrorResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Spring-Security工具类
 * 部分方法仅可以在默认配置下使用
 *
 * @author chen
 * @since 2018/12/24 23:34.
 */
public class Securitys {

    /**
     * 获取认证对象
     *
     * @return 认证对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取完全认证的认证对象
     *
     * @return 认证对象
     */
    public static Authentication getFullyAuthenticatedAuthentication() {
        ApiAssert.isTrue(isFullyAuthenticated(), SecurityErrorResponse.accountNotFullyAuthenticated());
        return getAuthentication();
    }

    /**
     * 获取认证对象中的{@link SecurityUser}
     * 默认的{@link org.springframework.security.core.userdetails.UserDetailsService}获取的是这个对象
     *
     * @return SecurityUser实例
     */
    public static SecurityUser getAuthenticationSecurityUser() {
        Authentication authentication = getFullyAuthenticatedAuthentication();
        Object principal = authentication.getPrincipal();
        ApiAssert.isInstanceOf(SecurityUser.class, principal, SecurityErrorResponse.principalError());
        return (SecurityUser) principal;
    }

    /**
     * 获取认证对象中的{@link SysUserRolePermission}
     * 默认的{@link org.springframework.security.core.userdetails.UserDetailsService}获取的是这个对象
     *
     * @return SecurityUser实例原始内容
     */
    public static SysUserRolePermission getAuthenticationSecurityUserOriginal() {
        return getAuthenticationSecurityUser().getOriginal();
    }

    /**
     * 获取当前登录系统用户id
     *
     * @return 系统用户id
     */
    public static String getSysUserId() {
        return getAuthenticationSecurityUserOriginal().getId();
    }

    /**
     * 获取认证对象的详细信息{@link CustomWebAuthenticationDetails}
     * 默认使用了{@link CustomWebAuthenticationDetails}
     *
     * @return CustomWebAuthenticationDetails实例
     */
    public static CustomWebAuthenticationDetails getAuthenticationDetails() {
        Authentication authentication = getAuthentication();
        Object details = authentication.getDetails();
        ApiAssert.isInstanceOf(CustomWebAuthenticationDetails.class, details, SecurityErrorResponse.detailsError());
        return (CustomWebAuthenticationDetails) details;

    }


    /**
     * 是否为匿名用户
     *
     * @return 是/否
     */
    public static boolean isAnonymous() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        return AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass());
    }

    /**
     * 是否为记住我用户
     *
     * @return 是/否
     */
    public static boolean isRememberMe() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        return RememberMeAuthenticationToken.class.isAssignableFrom(authentication.getClass());
    }

    /**
     * 是否身份认证
     *
     * @return 是/否
     */
    public static boolean isAuthenticated() {
        return !isAnonymous();
    }

    /**
     * 是否完全的身份认证
     *
     * @return 是/否
     */
    public static boolean isFullyAuthenticated() {
        return !isAnonymous() && !isRememberMe();
    }

    /**
     * 是否完全的身份认证
     * 如果没有完全的身份认证抛出异常
     */
    public static void checkFullyAuthenticated() {
        ApiAssert.isTrue(isFullyAuthenticated(), SecurityErrorResponse.accountNotFullyAuthenticated());
    }


    /**
     * 是否有权限
     *
     * @param permission 权限名
     * @return 是/否
     */
    public static boolean hasPermission(String permission) {
        return hasPermission(new String[]{permission});

    }

    /**
     * 是否有权限集合
     *
     * @param permissionCollection 权限名集合
     * @return 是/否
     */
    public static boolean hasPermission(Collection<String> permissionCollection) {
        return hasPermission(permissionCollection != null ? permissionCollection.toArray(new String[0]) : null);
    }

    /**
     * 是否有权限数组
     *
     * @param permissions 权限名数组
     * @return 是/否
     */
    public static boolean hasPermission(String... permissions) {
        SecurityUser securityUser = getAuthenticationSecurityUser();
        SysUserRolePermission original = securityUser.getOriginal();
        List<SysUserRolePermission.SysRolePermission> sysRolePermissionList = original.getSysRolePermissionList();
        if (CollectionUtils.isEmpty(sysRolePermissionList)) {
            return false;
        }
        return sysRolePermissionList.stream()
                .filter(Objects::nonNull)
                .filter(sysRolePermission -> CollectionUtils.isNotEmpty(sysRolePermission.getSysPermissionList()))
                .flatMap(sysRolePermission -> sysRolePermission.getSysPermissionList().stream())
                .filter(Objects::nonNull)
                .map(SysPermission::getName)
                .allMatch(sysPermissionString -> StringUtils.equalsAny(sysPermissionString, permissions));

    }

    /**
     * 是否有权限
     * 如果没有权限抛出异常
     *
     * @param permission 权限名
     */
    public static void checkPermission(String permission) {
        ApiAssert.isTrue(hasPermission(permission), SecurityErrorResponse.notPermission(permission));
    }

    /**
     * 是否有权限集合
     * 如果没有权限抛出异常
     *
     * @param permissionCollection 权限集合
     */
    public static void checkPermission(Collection<String> permissionCollection) {
        ApiAssert.isTrue(hasPermission(permissionCollection), SecurityErrorResponse.notPermission(permissionCollection));
    }

    /**
     * 是否有权限数组
     * 如果没有权限抛出异常
     *
     * @param permissions 权限名数组
     */
    public static void checkPermission(String... permissions) {
        ApiAssert.isTrue(hasPermission(permissions), SecurityErrorResponse.notPermission(permissions));
    }

    /**
     * 是否有角色
     *
     * @param role 角色名
     * @return 是/否
     */
    public static boolean hasRole(String role) {
        return hasRole(new String[]{role});
    }


    /**
     * 是否有角色集合
     *
     * @param roleCollection 角色名集合
     * @return 是/否
     */
    public static boolean hasRole(Collection<String> roleCollection) {
        return hasRole(roleCollection != null ? roleCollection.toArray(new String[0]) : null);
    }

    /**
     * 是否有角色数组
     *
     * @param roles 角色名数组
     * @return 是/否
     */
    public static boolean hasRole(String... roles) {
        SecurityUser securityUser = getAuthenticationSecurityUser();
        SysUserRolePermission original = securityUser.getOriginal();
        List<SysUserRolePermission.SysRolePermission> sysRolePermissionList = original.getSysRolePermissionList();
        if (CollectionUtils.isEmpty(sysRolePermissionList)) {
            return false;
        }
        return sysRolePermissionList.stream()
                .filter(Objects::nonNull)
                .map(SysUserRolePermission.SysRolePermission::getName)
                .allMatch(sysPermissionString -> StringUtils.equalsAny(sysPermissionString, roles));
    }

    /**
     * 是否有角色
     * 如果没有角色抛出异常
     *
     * @param role 角色名
     */
    public static void checkRole(String role) {
        ApiAssert.isTrue(hasRole(role), SecurityErrorResponse.notRole(role));
    }

    /**
     * 是否有角色集合
     * 如果没有角色抛出异常
     *
     * @param roleCollection 角色名集合
     */
    public static void checkRole(Collection<String> roleCollection) {
        ApiAssert.isTrue(hasRole(roleCollection), SecurityErrorResponse.notRole(roleCollection));
    }

    /**
     * 是否有角色数组
     * 如果没有角色抛出异常
     *
     * @param roles 角色名数组
     */
    public static void checkRole(String... roles) {
        ApiAssert.isTrue(hasRole(roles), SecurityErrorResponse.notRole(roles));
    }

    /**
     * 获取系统用户等级
     *
     * @return 等级(0是管理员, 数值越小级别越大)
     */
    public static int getSysUserLevel() {
        SecurityUser securityUser = getAuthenticationSecurityUser();
        return securityUser.getOriginal().getLevel();
    }

    /**
     * 是否是超级管理员
     *
     * @return 是/否
     */
    public static boolean isSuperAdministrator() {
        return getSysUserLevel() == 0;
    }

    /**
     * 是否是超级管理员
     * 如果不是超级管理员抛出异常
     */
    public static void checkSuperAdministrator() {
        ApiAssert.isTrue(isSuperAdministrator(), SecurityErrorResponse.notSuperAdministrator());
    }


}
