package com.project.gva.repository;

import com.project.gva.entity.ReadDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadDeviceRepository extends JpaRepository<ReadDevice, String> {

    //    Optional<ReadDevice> findByDeviceIdEquals(String logDevice);
    Page<ReadDevice> findAllByLogDeviceEquals(String device, Pageable pageable);
}
