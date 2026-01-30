package com.example.dgh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dgh.dto.AdminUserCreateRequest;
import com.example.dgh.dto.AdminUserResponse;
import com.example.dgh.dto.AdminUserUpdateRequest;
import com.example.dgh.entity.User;
import com.example.dgh.entity.UserRole;
import com.example.dgh.mapper.UserMapper;
import com.example.dgh.util.PasswordUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserMapper userMapper;

    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<AdminUserResponse> list() {
        return userMapper.selectList(new LambdaQueryWrapper<User>())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @PostMapping
    public AdminUserResponse create(@Valid @RequestBody AdminUserCreateRequest request) {
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
        return toResponse(userMapper.selectById(user.getId()));
    }

    @PatchMapping("/{id}")
    public AdminUserResponse update(@PathVariable String id, @RequestBody AdminUserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "人员不存在");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            long phoneExists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())
                .ne(User::getId, id));
            if (phoneExists > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "手机号已被占用");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getRoleCode() != null && !request.getRoleCode().isBlank()) {
            try {
                user.setRoleCode(UserRole.fromValue(request.getRoleCode()));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色参数不合法");
            }
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        userMapper.updateById(user);
        return toResponse(userMapper.selectById(id));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
            user.getId(),
            user.getUserId(),
            user.getPhone(),
            user.getName(),
            user.getRoleCode(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
