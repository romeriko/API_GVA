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
@Table(name = "ACTUALIZACION")
public class UpdateEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "VERSION")
    private String version;

    @Column(name = "FECHA_DE_CREACION")
    private Date creationDate;

    @Column(name = "COMENTARIO")
    private String comments;

    @ManyToOne
    @JoinColumn(name = "TIPO_DE_DISPOSITIVO")
    private DeviceTypeEntity type;

    @Column(name = "NAME")
    private String name;

    @ManyToOne
    @JoinColumn(name = "VALIDATION")
    private ValidationEntity validation;

    @PreUpdate
    public void preUpdate() {
        throw new ForbiddenException("This registry cannot be updated");
    }

    public String getCreationDate() {
        return creationDate.toString();
    }

    public String getDeviceType() {
        if (type == null)
            return "- - -";
        return type.getDescription();
    }

    public String getValidationId() {
        if (validation == null)
            return "- - -";
        return validation.getUuid();
    }
}
