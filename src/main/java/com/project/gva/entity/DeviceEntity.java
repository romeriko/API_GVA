package com.project.gva.entity;


import com.project.gva.model.Types;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Data
@Entity
@Table(name = "DISPOSITIVO")
public class DeviceEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NOMBRE_DISPOSITIVO")
    private String name;

    @Column(name = "DIRECCION_IP")
    private String ip;

    @Column(name = "FECHA_DE_CONEXION")
    private Date lastConnection;

    @Column(name = "VERSION")
    private String version;

    @Column(name = "ESTADO")
    @Enumerated(value = EnumType.STRING)
    private Types.DeviceStatus status;

    @Column(name = "USUARIO")
    private String username;

    @ManyToOne
    @JoinColumn(name = "TIPO_DE_DISPOSITIVO")
    private DeviceTypeEntity type;

    @Column(name = "FECHA_DE_ACTUALIZACION")
    private Date lastUpdate;

    @Column(name = "AUTOMATIC_UPDATE")
    private Boolean automaticUpdated;

    @Column(name = "ALLOW_CORRUPT")
    private Boolean allowCorrupt;

    @Column(name = "CLAIM")
    private Boolean claim;

    @Transient
    private String typeName;

    @Transient
    private boolean old;

    public String getType() {
        if (Objects.nonNull(type))
            return this.type.getDescription();
        return "";
    }

    public DeviceTypeEntity getTypeEntity() {
        return type;
    }

    public String getConnection() {
        return this.lastConnection.toString();
    }

    public String getUpdated() {
        return this.lastUpdate.toString();
    }

    public enum DeviceStatus {
        OFFLINE,
        ONLINE
    }

    @Override
    public String toString() {
        return "DeviceEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", lastConnection=" + lastConnection +
                ", version='" + version + '\'' +
                ", status=" + status +
                ", username='" + username + '\'' +
                ", type=" + type +
                ", lastUpdate=" + lastUpdate +
                ", automaticUpdated=" + automaticUpdated +
                ", allowCorrupt=" + allowCorrupt +
                ", claim=" + claim +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
