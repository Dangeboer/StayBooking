# 🏡 在线短租预订平台

一个基于 **Spring Boot** 的网页式短租平台，支持房东发布房源、租户浏览与搜索，并完成在线预订。项目通过多级缓存、分布式锁和云服务优化，实现高并发场景下的安全与性能保障。

## ✨ 功能特性

- **房源管理**：房东可发布、修改和删除房源信息  
- **在线预订**：租户可浏览房源、搜索并完成预订  
- **用户认证**：基于 JWT 的令牌式身份验证，保障 API 安全  
- **跨域与安全**：通过 Nginx 配置 CORS 与 SSL，支持 HTTPS 访问  
- **地理位置检索**：基于 PostGIS 的空间索引，实现按位置和商圈的房源搜索  
- **多地图源支持**：通过策略模式封装高德和谷歌地图 API，支持可扩展的多地图源调用  
- **智能推荐**：结合用户历史预订和当前位置，实时推荐高分房源  
- **高并发保障**：基于 Redisson 分布式锁，防止房源超卖  
- **多级缓存**：Redis + Caffeine 缓存机制，提升系统性能  
- **云端存储**：Google Cloud Storage 管理房源图片  
- **容器化与部署**：Docker 容器化数据库，服务部署至 Google Cloud Run  

## 🛠 技术栈

- **后端框架**：Spring Boot, Spring Security, Spring Cloud OpenFeign, Spring Data JPA  
- **数据库**：PostgreSQL + PostGIS  
- **缓存**：Redis, Caffeine  
- **并发控制**：Redisson  
- **网关 & 部署**：Nginx, Docker, Google Cloud Run  
- **云服务**：Google Cloud Storage  
- **其他**：Gradle 构建，JUnit 单元测试  

## 📐 系统架构

```mermaid
graph TD
    Client[浏览器/移动端] -->|HTTPS| Nginx
    Nginx -->|REST API| Backend[Spring Boot Service]
    Backend -->|OpenFeign| Maps[地图 API: 高德 / Google]
    Backend -->|JPA| PostgreSQL[(PostgreSQL + PostGIS)]
    Backend -->|Cache| Redis[(Redis + Caffeine)]
    Backend -->|Lock| Redisson[(分布式锁)]
    Backend -->|Media| GCS[(Google Cloud Storage)]
