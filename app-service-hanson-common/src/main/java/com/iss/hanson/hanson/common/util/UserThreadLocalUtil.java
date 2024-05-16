package com.iss.hanson.hanson.common.util;

import com.iss.hanson.hanson.common.bo.UserInfoBo;

/**
 * @author: HansonHu
 * @date: 2022-09-15 10:59
 **/
public class UserThreadLocalUtil {

    /**
     * 保存用户对象的ThreadLocal  在拦截器操作 添加、删除相关用户数据
     */
    private static final ThreadLocal<UserInfoBo> userThreadLocal = new ThreadLocal<UserInfoBo>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     * @param user
     */
    public static void addCurrentUser(UserInfoBo user){
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户方法
     */
    public static UserInfoBo getCurrentUser(){
        return userThreadLocal.get();
    }


    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove(){
        userThreadLocal.remove();
    }
}


