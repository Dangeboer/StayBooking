package com.laioffer.staybooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CORS（跨域资源共享）过滤器
 * 
 * 作用：解决前后端分离架构中的跨域问题
 * 当前端应用（如运行在 http://localhost:3000）试图访问后端API（如运行在 http://localhost:8080）时，
 * 浏览器会阻止这种跨域请求，除非后端明确允许。
 * 
 * CORS是浏览器的安全机制，防止恶意网站访问其他域名的资源。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 设置最高优先级，确保CORS过滤器在其他过滤器之前执行，也是因为有这个，所以不需要在AppConfig里配置CORS过滤器了
public class StaybookingCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        
        // 1. 允许所有域名访问（生产环境中应该指定具体域名）
        // 解决跨域问题的核心：告诉浏览器允许哪些域名访问这个API
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        
        // 2. 允许的HTTP方法
        // 指定前端可以使用哪些HTTP方法来访问API
        // POST: 创建资源（如注册用户、创建房源）
        // GET: 查询资源（如获取房源列表、搜索房源）
        // OPTIONS: 预检请求（浏览器自动发送，用于检查是否允许跨域）
        // DELETE: 删除资源（如删除房源、取消预订）
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        
        // 3. 允许的请求头
        // Authorization: 用于JWT token认证，前端需要在请求头中发送token
        // Content-Type: 用于指定请求体的内容类型（如application/json）
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        
        // 4. 处理OPTIONS预检请求
        // 浏览器在发送实际请求前会先发送OPTIONS请求来检查是否允许跨域
        // 如果请求方法是OPTIONS，直接返回200状态码，告诉浏览器允许跨域
        if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            // 如果不是OPTIONS请求，继续执行过滤器链中的下一个过滤器
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}

/*
 * 使用场景示例：
 * 
 * 前端代码：
 * fetch('http://localhost:8080/listings', {
 *     method: 'GET',
 *     headers: {
 *         'Authorization': 'Bearer ' + token,
 *         'Content-Type': 'application/json'
 *     }
 * })
 * 
 * 如果没有这个CORS过滤器，浏览器会报错：
 * "Access to fetch at 'http://localhost:8080/listings' from origin 'http://localhost:3000' 
 *  has been blocked by CORS policy"
 * 
 * 安全考虑：
 * - 当前配置允许所有域名访问（*），适合开发环境
 * - 生产环境建议指定具体域名：response.setHeader("Access-Control-Allow-Origin", "https://yourdomain.com");
 * 
 * ========== CORS中的"域名"概念详解 ==========
 * 
 * 在CORS中，"域名"实际上指的是完整的源（Origin），包括：
 * - 协议（http/https）
 * - 域名（localhost, example.com等）
 * - 端口号（3000, 8080等）
 * 
 * 具体例子：
 * - 前端运行在：http://localhost:3000
 * - 后端运行在：http://localhost:8080
 * - 跨域原因：端口号不同（3000 ≠ 8080）
 * 
 * 浏览器判断跨域的逻辑：
 * 只要以下任一不同，就是跨域：
 * 1. 协议不同：http vs https
 * 2. 域名不同：localhost vs example.com  
 * 3. 端口不同：3000 vs 8080
 * 
 * 更精确的配置示例：
 * // 如果只想允许特定端口的前端访问：
 * response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
 * 
 * // 或者允许多个特定源：
 * response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000,http://localhost:3001");
 * 
 * 在开发环境中，主要就是端口号不同导致的跨域问题！
 */
