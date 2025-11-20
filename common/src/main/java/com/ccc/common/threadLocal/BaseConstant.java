package com.ccc.common.threadLocal;

import com.example.pojo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BaseConstant {

    public static java.lang.ThreadLocal<Integer> threadLocal = new java.lang.ThreadLocal<>();

    public static Integer getCurrentUser() {return threadLocal.get();}

    public static void setCurrentUser(int id) {threadLocal.set(id);}

    public static void removeCurrentUser() {threadLocal.remove();}
}
