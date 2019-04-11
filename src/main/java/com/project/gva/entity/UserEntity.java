package com.project.gva.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "USUARIO")
public class UserEntity {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String name;

    @JsonIgnore
    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TELEFONO")
    private String phone;

    @Column(name = "FECHA_DE_CONEXION")
    private Date connectionDate;

    @Column(name = "BLOCKED_UNTIL")
    private Date blockedUntil;

    @ManyToOne
    @JoinColumn(name = "PRIVILEGIO")
    private PrivilegeEntity privilege;

}
