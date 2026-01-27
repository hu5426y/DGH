package com.example.dgh.controller;

import com.example.dgh.dto.UserOptionResponse;
import com.example.dgh.entity.User;
import com.example.dgh.entity.UserRole;
import com.example.dgh.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserOptionResponse> list(@RequestParam(required = false, name = "role_code") String roleCode) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .eq(User::getIsActive, 1);
        if (roleCode != null && !roleCode.isBlank()) {
            try {
                wrapper.eq(User::getRoleCode, UserRole.fromValue(roleCode));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色参数不合法");
            }
        }
        return userMapper.selectList(wrapper)
            .stream()
            .map(user -> new UserOptionResponse(user.getId(), user.getName(), user.getPhone()))
            .collect(Collectors.toList());
    }
}
