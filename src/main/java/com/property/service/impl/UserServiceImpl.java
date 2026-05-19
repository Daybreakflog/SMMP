package com.property.service.impl;

import com.property.service.UserService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.RolePermission;
import com.property.entity.User;
import com.property.entity.UserRole;
import com.property.mapper.RolePermissionMapper;
import com.property.mapper.UserMapper;
import com.property.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public User findById(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User findByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User findByPhone(String phone) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public List<String> findRolesByUserId(String userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        return userRoles.stream().map(UserRole::getRoleCode).toList();
    }

    public List<String> findPermissionsByUserId(String userId) {
        List<String> roleCodes = findRolesByUserId(userId);
        if (roleCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<RolePermission> rolePerms = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleCode, roleCodes));
        if (rolePerms.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePerms.stream()
                .map(RolePermission::getPermissionCode)
                .distinct()
                .toList();
    }
}
