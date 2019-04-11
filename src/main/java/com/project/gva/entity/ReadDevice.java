package com.project.gva.entity;

import com.project.gva.model.Types;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DEVICE_STATUS")
public class ReadDevice {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "DEVICE_ID")
    private String logDevice;

    @Column(name = "DEVICE_NAME")
    private String name;

    @Column(name = "STATUS")
    @Enumerated(value = EnumType.STRING)
    private Types.DeviceStatus status;

    @Column(name = "UPDATED_TIME")
    private Date date;
}
