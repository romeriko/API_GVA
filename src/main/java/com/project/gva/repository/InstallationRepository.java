package com.project.gva.repository;

import com.project.gva.entity.DeviceEntity;
import com.project.gva.entity.InstallationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstallationRepository extends JpaRepository<InstallationEntity, String> {

    @Query(value = "UPDATE ACTUALIZACION A SET A.CURRENTLY_INSTALLED = 0", nativeQuery = true)
    void clear();
    InstallationEntity findByCurrentlyInstalledEqualsAndDeviceEquals(boolean installed, DeviceEntity deviceEntity);

    List<InstallationEntity> findAllByCurrentlyInstalled(boolean installed);
}
