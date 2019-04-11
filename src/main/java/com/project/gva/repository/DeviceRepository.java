package com.project.gva.repository;

import com.project.gva.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {

    Optional<DeviceEntity> findByIdAndClaimTrue(String id);

    List<DeviceEntity> findAllByClaimTrue();
}
