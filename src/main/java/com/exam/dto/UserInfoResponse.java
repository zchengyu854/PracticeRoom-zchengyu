package com.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息响应数据")
public class UserInfoResponse {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "管理员")
    private String realName;

    @Schema(description = "用户角色", example = "admin", allowableValues = {"admin", "teacher", "student"})
    private String role;

    @Schema(description = "用户状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-15 10:00:00")
    private String createTime;
}
