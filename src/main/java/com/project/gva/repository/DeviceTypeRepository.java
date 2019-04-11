package com.project.gva.repository;

import com.project.gva.entity.DeviceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTypeRepository extends JpaRepository<DeviceTypeEntity, String> {

    Optional<DeviceTypeEntity> findByDescriptionEquals(String desc);
}
