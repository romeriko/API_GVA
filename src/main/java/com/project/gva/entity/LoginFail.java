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
@Table(name = "LOGIN_FAIL")
public class LoginFail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATE")
    private Date date;

    @ManyToOne
    @JoinColumn(name = "USER")
    private UserEntity user;

    @Column(name = "REMOTE_IP")
    private String ip;
}
