package com.example.dgh.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dgh.dto.LoginCodeRequest;
import com.example.dgh.dto.LoginPasswordRequest;
import com.example.dgh.dto.LoginResponse;
import com.example.dgh.dto.RegisterRequest;
import com.example.dgh.dto.SendLoginCodeRequest;
import com.example.dgh.entity.User;
import com.example.dgh.entity.UserRole;
import com.example.dgh.mapper.UserMapper;
import com.example.dgh.util.PasswordUtil;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final AuthTokenStore tokenStore;

    public AuthService(UserMapper userMapper, StringRedisTemplate redisTemplate, AuthTokenStore tokenStore) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.tokenStore = tokenStore;
    }

    public LoginResponse register(RegisterRequest request) {
        long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getUserId, request.getUserId())
            .or()
            .eq(User::getPhone, request.getPhone()));
        if (exists > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户ID或手机号已存在");
        }

        UserRole role;
        try {
            role = UserRole.fromValue(request.getRoleCode());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色参数不合法");
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setRoleCode(role);
        user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
        user.setIsActive(1);
        userMapper.insert(user);
        return buildLoginResponse(user);
    }

    public LoginResponse loginWithPassword(LoginPasswordRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUserId, request.getUserId()));
        if (user == null || user.getIsActive() == null || user.getIsActive() == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        return buildLoginResponse(user);
    }

    public void sendLoginCode(SendLoginCodeRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getPhone, request.getPhone()));
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "手机号未注册");
        }
        String cooldownKey = "login:code:cooldown:" + request.getPhone();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
        }
        String code = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
        String codeKey = "login:code:" + request.getPhone();
        redisTemplate.opsForValue().set(codeKey, code, Duration.ofMinutes(1));
        redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(1));
        LOGGER.info("Login verification code sent. phone={}, code={}", request.getPhone(), code);
    }

    public LoginResponse loginWithCode(LoginCodeRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getPhone, request.getPhone()));
        if (user == null || user.getIsActive() == null || user.getIsActive() == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "验证码错误或已过期");
        }
        String codeKey = "login:code:" + request.getPhone();
        String cachedCode = redisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "验证码错误或已过期");
        }
        redisTemplate.delete(codeKey);
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = tokenStore.issueToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getUserId(), user.getName(), user.getPhone(), user.getRoleCode().getValue());
    }
}
