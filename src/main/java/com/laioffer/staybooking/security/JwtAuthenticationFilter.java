package com.laioffer.staybooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 这个类是一个 Spring Security 过滤器组件，它会拦截每个请求并根据 JWT 进行身份认证。
// 主要功能是从请求头中提取 JWT，验证它，并将认证信息存储到 SecurityContext 中
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtHandler jwtHandler;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtHandler jwtHandler,
            UserDetailsService userDetailsService
    ) {
        this.jwtHandler = jwtHandler;
        this.userDetailsService = userDetailsService;
    }

    // 这里重写了 OncePerRequestFilter 里的 doFilterInternal() 方法，其实能算模板方法模式
    // OncePerRequestFilter 本身是 Spring 提供的抽象类，它已经实现了 doFilter 方法，并且在里面写好了整个过滤器的“算法流程”（比如保证每个请求只调用一次过滤逻辑、异常处理、调用链的传递）。
    // 其中某一步骤——具体如何处理请求——被定义为一个抽象方法 doFilterInternal，留给子类去实现。
    // 你的 JwtAuthenticationFilter 就是具体的子类，它通过重写 doFilterInternal 来实现自己的业务逻辑（JWT 校验、认证信息注入）。
    // 在你的 JwtAuthenticationFilter 里，你没有重写 doFilter，而是重写了 doFilterInternal。但项目里最终启用的仍然是 doFilter ——因为 Servlet 过滤器规范要求调用的是 doFilter 方法。
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String jwt = getJwtFromRequest(request); // 从请求头中提取 JWT。如果请求头中没有 JWT 或格式不正确，则直接继续过滤链的下一步
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }
        final String username = jwtHandler.parsedUsername(jwt); // 解析 JWT，从中提取用户名
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username); // 根据提取到的用户名从 UserDetailsService 加载用户详细信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // 这个对象是 Spring Security 用来表示用户认证信息的标准方式
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 设置认证详情。会构建一些请求级别的细节信息（如 IP 地址、session 等），并把它们设置到认证对象中
        SecurityContextHolder.getContext().setAuthentication(authentication); // 将认证信息存储到 SecurityContext 中，Spring Security 会使用这个上下文来进行后续的权限验证
        filterChain.doFilter(request, response); // 继续执行过滤链中的下一个过滤器或目标资源。
    }

    // 从 HTTP 请求头的 Authorization 字段中提取 JWT
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER); // 根据常规约定，JWT 通常使用 "Bearer " 前缀，所以代码检查头部是否以 Bearer 开头
        if (bearerToken == null || !bearerToken.startsWith(PREFIX)) { // 如果格式正确，去除 Bearer 前缀并返回 JWT，否则返回 null
            return null;
        }
        return bearerToken.substring(7);
    }
}
