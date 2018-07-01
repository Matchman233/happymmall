package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

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

    // 定义排序
    public interface ProductListOrderBy{
        Set<String> PRICE_DESC_ASC = Sets.newHashSet("price_desc","price_asc");
    }

    public enum productStatusEnum {
        ON_SALE(1, "在线");

        private Integer code;
        private String desc;

        public String getDesc() {
            return desc;
        }

        public Integer getCode() {
            return code;
        }

        productStatusEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }


}
