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
@Table(name = "AUTH")
public class AuthToken {

    @Id
    @Column(name = "TOKEN_ID")
    private String token;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserEntity user;


    @Column(name = "VALID")
    private boolean valid;

    @Column(name = "CREATED")
    private Date created;

    @Transient
    private int code;
}
