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
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final Duration LOGIN_CODE_TTL = Duration.ofMinutes(1);
    private static final Duration LOGIN_CODE_COOLDOWN = Duration.ofMinutes(1);
    private static final ConcurrentHashMap<String, Instant> IN_MEMORY_COOLDOWN = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CodeEntry> IN_MEMORY_CODES = new ConcurrentHashMap<>();
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
        if (role == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号需由管理员在人员管理中创建");
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
        String code = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
        try {
            String cooldownKey = "login:code:cooldown:" + request.getPhone();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
            }
            String codeKey = "login:code:" + request.getPhone();
            redisTemplate.opsForValue().set(codeKey, code, LOGIN_CODE_TTL);
            redisTemplate.opsForValue().set(cooldownKey, "1", LOGIN_CODE_COOLDOWN);
        } catch (RedisConnectionFailureException ex) {
            if (isCooldownActive(request.getPhone())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
            }
            setCooldown(request.getPhone(), LOGIN_CODE_COOLDOWN);
            setCode(request.getPhone(), code, LOGIN_CODE_TTL);
            LOGGER.warn("Redis unavailable; using in-memory login code storage.");
        }
        LOGGER.info("Login verification code sent. phone={}, code={}", request.getPhone(), code);
    }

    public LoginResponse loginWithCode(LoginCodeRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getPhone, request.getPhone()));
        if (user == null || user.getIsActive() == null || user.getIsActive() == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "验证码错误或已过期");
        }
        String cachedCode;
        try {
            String codeKey = "login:code:" + request.getPhone();
            cachedCode = redisTemplate.opsForValue().get(codeKey);
        } catch (RedisConnectionFailureException ex) {
            cachedCode = getCode(request.getPhone());
            LOGGER.warn("Redis unavailable; using in-memory login code storage.");
        }
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "验证码错误或已过期");
        }
        try {
            String codeKey = "login:code:" + request.getPhone();
            redisTemplate.delete(codeKey);
        } catch (RedisConnectionFailureException ex) {
            deleteCode(request.getPhone());
        }
        return buildLoginResponse(user);
    }

    private boolean isCooldownActive(String phone) {
        Instant expiresAt = IN_MEMORY_COOLDOWN.get(phone);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            IN_MEMORY_COOLDOWN.remove(phone);
            return false;
        }
        return true;
    }

    private void setCooldown(String phone, Duration ttl) {
        IN_MEMORY_COOLDOWN.put(phone, Instant.now().plus(ttl));
    }

    private void setCode(String phone, String code, Duration ttl) {
        IN_MEMORY_CODES.put(phone, new CodeEntry(code, Instant.now().plus(ttl)));
    }

    private String getCode(String phone) {
        CodeEntry entry = IN_MEMORY_CODES.get(phone);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            IN_MEMORY_CODES.remove(phone);
            return null;
        }
        return entry.code();
    }

    private void deleteCode(String phone) {
        IN_MEMORY_CODES.remove(phone);
    }

    private record CodeEntry(String code, Instant expiresAt) {}

    private LoginResponse buildLoginResponse(User user) {
        String token = tokenStore.issueToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getUserId(), user.getName(), user.getPhone(), user.getRoleCode().getValue());
    }
}
