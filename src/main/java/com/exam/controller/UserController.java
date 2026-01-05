package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.dto.RegisterRequest;
import com.exam.dto.UserInfoResponse;
import com.exam.entity.User;
import com.exam.interceptor.LoginInterceptor;
import com.exam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户相关操作，包括登录认证、权限验证等功能")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户通过用户名和密码进行登录验证，返回用户信息和token")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        String token = UUID.randomUUID().toString();
        LoginInterceptor.tokenUserMap.put(token, user);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setToken(token);

        return Result.success(response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册新账号")
    public Result<Void> register(@RequestBody @Valid RegisterRequest registerRequest) {
        boolean success = userService.register(registerRequest);
        if (!success) {
            return Result.error(400, "用户名已存在");
        }
        return Result.success("注册成功");
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被注册")
    public Result<Boolean> checkUsername(@Parameter(description = "用户名") @RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            LoginInterceptor.tokenUserMap.remove(token);
        }
        return Result.success("退出成功");
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "根据token获取当前登录用户的信息")
    public Result<UserInfoResponse> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        String token = authorization.substring(7);
        User user = LoginInterceptor.tokenUserMap.get(token);
        if (user == null) {
            return Result.error(401, "登录已过期");
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);

        return Result.success(response);
    }

    @GetMapping("/check-admin/{userId}")
    @Operation(summary = "检查管理员权限", description = "验证指定用户是否具有管理员权限")
    public Result<Boolean> checkAdmin(@Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean isAdmin = userService.isAdmin(userId);
        return Result.success(isAdmin);
    }
} 