package com.project.gva.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TIPO_DE_DISPOSITIVO")
public class DeviceTypeEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NOMBRE_DISPOSITIVO")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
