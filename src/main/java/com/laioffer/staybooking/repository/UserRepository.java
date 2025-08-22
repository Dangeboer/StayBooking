package com.laioffer.staybooking.repository;

import com.laioffer.staybooking.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository; // JPA repository

// 操作对象是UserEntity，long是primary key的数据类型
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUsername(String username);

    // JPA的命名规则，只要符合这种规则，就可以这样操作数据库了
    boolean existsByUsername(String username);
}