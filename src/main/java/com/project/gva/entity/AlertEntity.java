package com.project.gva.entity;

import com.project.gva.model.Types;
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
@Table(name = "ALERTA")
public class AlertEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "DEVICE")
    private String device;

    @Column(name = "TOPIC")
    @Enumerated(value = EnumType.STRING)
    private Types.MessageTopic topic;

    @Column(name = "STATUS")
    @Enumerated(value = EnumType.STRING)
    private Types.MessageStatus status;

    @Column(name = "DOCUMENT")
    private String document;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "DATE")
    private Date date;

    public String getStyle() {
        return status.style();
    }

}
