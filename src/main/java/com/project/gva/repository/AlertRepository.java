package com.project.gva.repository;

import com.project.gva.entity.AlertEntity;
import com.project.gva.model.Types;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<AlertEntity, String> {

    AlertEntity findByMessageEquals(String message);

    List<AlertEntity> findAllByTopicEquals(Types.MessageTopic topic);

    List<AlertEntity> findAllByOrderByDateDesc();
}
