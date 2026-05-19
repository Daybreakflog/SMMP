package com.property.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.property.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userService.findPermissionsByUserId(loginId.toString());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userService.findRolesByUserId(loginId.toString());
    }
}
