package com.project.gva.repository;

import com.project.gva.entity.DeviceTypeEntity;
import com.project.gva.entity.UpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UpdateRepository extends JpaRepository<UpdateEntity, Long> {

    List<UpdateEntity> findAllByTypeEqualsAndVersionGreaterThan(DeviceTypeEntity deviceType, String version);
}
