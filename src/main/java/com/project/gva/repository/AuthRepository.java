package com.project.gva.repository;

import com.project.gva.entity.AuthToken;
import com.project.gva.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<AuthToken, String> {

    AuthToken findByUserEqualsAndValidEquals(UserEntity user, boolean valid);
    AuthToken findByTokenEqualsAndValidEquals(String token, boolean valid);
}
