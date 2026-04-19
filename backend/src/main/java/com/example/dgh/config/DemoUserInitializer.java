package com.example.dgh.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dgh.entity.User;
import com.example.dgh.entity.UserRole;
import com.example.dgh.mapper.UserMapper;
import com.example.dgh.util.PasswordUtil;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoUserInitializer {
    @Bean
    public ApplicationRunner initDemoUsers(UserMapper userMapper) {
        return args -> {
            List<DemoUserSeed> demoUsers = List.of(
                new DemoUserSeed("77777777-aaaa-aaaa-aaaa-777777777777", "demo_admin", "18800001001", "演示管理员", UserRole.ADMIN),
                new DemoUserSeed("88888888-aaaa-aaaa-aaaa-888888888888", "demo_student", "18800001002", "演示学生", UserRole.USER),
                new DemoUserSeed("99999999-aaaa-aaaa-aaaa-999999999999", "demo_repairer", "18800001003", "演示维修工", UserRole.REPAIRER)
            );

            for (DemoUserSeed seed : demoUsers) {
                long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUserId, seed.userId())
                    .or()
                    .eq(User::getPhone, seed.phone()));
                if (exists > 0) {
                    continue;
                }

                User user = new User();
                user.setId(seed.id());
                user.setUserId(seed.userId());
                user.setPhone(seed.phone());
                user.setName(seed.name());
                user.setRoleCode(seed.roleCode());
                user.setPasswordHash(PasswordUtil.hash("123456"));
                user.setIsActive(1);
                userMapper.insert(user);
            }
        };
    }

    private record DemoUserSeed(String id, String userId, String phone, String name, UserRole roleCode) {}
}
