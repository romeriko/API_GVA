package com.project.gva.entity;

import com.project.gva.exception.ForbiddenException;
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
@Table(name = "VALIDATION")
public class ValidationEntity {

    @Id
    @Column(name = "UUID")
    private String uuid;

    @Column(name = "UPDATE_NAME")
    private String name;

    @Column(name = "SOURCE")
    private String source;

    @ManyToOne
    @JoinColumn(name = "DEVICE_TYPE")
    private DeviceTypeEntity type;

    @Column(name = "CRC32")
    private String crc32;

    @Column(name = "BYTES")
    private long bytes;

    @Column(name = "CKSUM")
    private String ckSum;

    @Column(name = "SHA256")
    private String sha256;

    @Column(name = "DATE")
    private Date date;

    public String getType() {
        return type.getDescription();
    }

    @PreUpdate
    public void preUpdate() {
        throw new ForbiddenException("This registry cannot be updated");
    }

    public boolean isValid(ValidationEntity validation) {
        return validation.ckSum.equals(ckSum) && validation.sha256.equals(sha256) && validation.bytes == bytes && validation.crc32.equals(crc32) && validation.type.equals(type);
    }
}
