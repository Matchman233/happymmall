package com.mmall.common;

/**
 * Created by apple on 2018/6/18.
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    // 定义用户的权限问题
    public interface Role {
        int Role_CUSTOMER = 0; // 普通用户
        int Role_ADMIN = 1; // 管理者
    }


}
