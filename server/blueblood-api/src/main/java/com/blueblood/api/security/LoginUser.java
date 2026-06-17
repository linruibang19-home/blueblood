package com.blueblood.api.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录用户（存入 SecurityContext 的 principal）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;

    private String username;

    /** 角色：USER / ADMIN 等 */
    private String role;
}
