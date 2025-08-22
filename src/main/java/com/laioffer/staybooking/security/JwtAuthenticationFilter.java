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
