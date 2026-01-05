package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.dto.RegisterRequest;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户Service实现类
 * 实现用户相关的业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .eq("password", password)
                   .eq("status", "active");
        return this.getOne(queryWrapper);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = this.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    @Override
    public boolean register(RegisterRequest registerRequest) {
        User existingUser = getUserByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            return false;
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setRealName(registerRequest.getRealName());
        user.setRole(registerRequest.getRole());
        user.setStatus("active");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        return this.save(user);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public User getUserByToken(String token) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", token)
                   .eq("status", "active");
        return this.getOne(queryWrapper);
    }
} 