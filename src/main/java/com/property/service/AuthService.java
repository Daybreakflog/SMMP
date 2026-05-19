package com.property.service;

import com.property.entity.User;
import com.property.dto.request.LoginDTO;
import com.property.dto.request.RefreshDTO;
import com.property.dto.request.SmsLoginDTO;
import com.property.dto.response.LoginVO;
import com.property.dto.response.TokenVO;
import com.property.dto.response.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    LoginVO smsLogin(SmsLoginDTO dto);

    TokenVO refresh(RefreshDTO dto);

    void logout();

    UserVO me();
}
