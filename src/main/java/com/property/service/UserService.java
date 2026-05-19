package com.property.service;

import com.property.entity.User;
import java.util.List;

public interface UserService {

    User findById(String id);

    User findByUsername(String username);

    User findByPhone(String phone);

    List<String> findRolesByUserId(String userId);

    List<String> findPermissionsByUserId(String userId);
}
