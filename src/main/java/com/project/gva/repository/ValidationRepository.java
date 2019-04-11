package com.project.gva.repository;

import com.project.gva.entity.ValidationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepository extends JpaRepository<ValidationEntity, String> {
    Optional<ValidationEntity> findByNameEquals(String name);
}
