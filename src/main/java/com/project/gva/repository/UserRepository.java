package com.project.gva.repository;

import com.project.gva.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    public UserEntity findByEmailEqualsAndBlockedUntilLessThanEqual(String email, Date date);
}
