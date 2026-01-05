package com.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "用户注册请求参数")
public class RegisterRequest {

    @Schema(description = "用户名", example = "student001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在3到20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "真实姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度在2到20个字符")
    private String realName;

    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @Schema(description = "用户角色", example = "student", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"student", "teacher", "admin"})
    @NotBlank(message = "请选择角色")
    private String role;
}
