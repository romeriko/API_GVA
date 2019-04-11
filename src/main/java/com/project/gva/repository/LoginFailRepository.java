package com.project.gva.repository;

import com.project.gva.entity.LoginFail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface LoginFailRepository extends JpaRepository<LoginFail, Long> {
    List<LoginFail> findAllByIpEqualsAndDateGreaterThanEqual(String ip, Date date);
}
