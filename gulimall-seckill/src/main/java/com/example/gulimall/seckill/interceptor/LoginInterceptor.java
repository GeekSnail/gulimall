package com.example.gulimall.seckill.interceptor;

import com.example.common.constant.AuthConstant;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private AntPathMatcher pathMatcher = new AntPathMatcher();
    public static ThreadLocal<Map> threadLocal= new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
//        if ("gulimall-seckill".equals(request.getHeader("feignTarget")))
//            return true;
        String uri = request.getRequestURI();
        if (pathMatcher.match("/kill", uri)) {
            Map<String,Object> user = (Map<String, Object>) session.getAttribute(AuthConstant.LOGIN_USER);
            if (user == null) {
                session.setAttribute("msg", "请先登录");
                String qs = request.getQueryString();
                response.sendRedirect("//auth.gulimall.com/login.html?redirect_url=http://seckill.gulimall.com"+ uri + (qs==null?"":"?"+qs));
                return false;
            }
            threadLocal.set(user);
        }
        return true;
    }
}
