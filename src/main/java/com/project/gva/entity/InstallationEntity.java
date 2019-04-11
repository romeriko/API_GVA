package com.project.gva.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "INSTALATION")
public class InstallationEntity {

    @Id
    @Column(name = "UUID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "DEVICE")
    private DeviceEntity device;

    @ManyToOne
    @JoinColumn(name = "ACTUALIZACION")
    private UpdateEntity update;

    @Column(name = "LAST_UPDATE")
    private Date lastUpdated;

    @Column(name = "FIRST_UPDATE")
    private Date firstUpdated;

    @ManyToOne
    @JoinColumn(name = "FIRST_UPDATE_VERSION")
    private UpdateEntity firstVersion;

    @ManyToOne
    @JoinColumn(name = "VALIDATION")
    private ValidationEntity validation;

    @Column(name = "IS_VALID")
    private boolean valid;

    @Column(name = "CURRENTLY_INSTALLED")
    private boolean currentlyInstalled;

    public String getValid() {
        return valid ? "VÁLIDO" : "NO VÁLIDA";
    }

    public String getLastUpdated() {
        return lastUpdated.toString();
    }

    public String getDeviceId() {
        return device.getId();
    }

    public String getUpdateName() {
        return update.getName();
    }

    public String getUpdateId() {
        return update.getId().toString();
    }

    public String getUpdateVersion() {
        return update.getVersion();
    }

    public String getValidation() {
        return validation.getUuid();
    }

    public DeviceEntity getDevice() {
        return device;
    }
}
