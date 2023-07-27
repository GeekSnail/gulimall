package com.example.gulimall.cart.interceptor;

import com.example.common.constant.AuthConstant;
import com.example.gulimall.cart.vo.UserInfo;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Map<String, Object> userMap = (Map<String, Object>) session.getAttribute(AuthConstant.LOGIN_USER);
        UserInfo userInfo = new UserInfo();
        if (userMap != null) {
            userInfo.setUserId(Long.parseLong(userMap.get("id").toString()));
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            //3.用户从cookie取user-key
            for (Cookie cookie: cookies) {
                if ("user-key".equals(cookie.getName())) {
                    userInfo.setUserKey(cookie.getValue());
                    userInfo.setCookieHad(true);
                }
            }
        }
        //1.临时用户生成user-key
        if (StringUtils.isEmpty(userInfo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfo.setUserKey(uuid);
        }
        threadLocal.set(userInfo);
        return true;
    }
    //让浏览器保存个cookie
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfo userInfo = threadLocal.get();
        if (!userInfo.isCookieHad()) {
            //2.临时用户存user-key到cookie
            Cookie cookie = new Cookie("user-key", userInfo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(60*60*24*30); //1 month
            response.addCookie(cookie);
        }
    }
}
