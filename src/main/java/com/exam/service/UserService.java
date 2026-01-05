package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.dto.RegisterRequest;
import com.exam.entity.User;

/**
 * 用户Service接口
 * 定义用户相关的业务方法
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User login(String username, String password);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 检查用户是否为管理员
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 是否注册成功
     */
    boolean register(RegisterRequest registerRequest);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return true表示已存在，false表示不存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 根据token获取用户信息
     * @param token 登录令牌
     * @return 用户信息
     */
    User getUserByToken(String token);
} 