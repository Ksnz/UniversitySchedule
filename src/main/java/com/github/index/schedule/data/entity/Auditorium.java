package com.github.index.schedule.data.entity;

import lombok.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "auditoriums")
@IdClass(AuditoriumKey.class)
@Getter
@Setter
@EqualsAndHashCode(exclude = {"scheduleEntries"})
@ToString(exclude = {"scheduleEntries"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Auditorium implements Serializable {

    transient static final int DEFAULT_CAPACITY = 30;

    @Id
    private int room;

    @Id
    private int housing;

    @Column(nullable = false)
    private int capacity;

    @OneToMany(mappedBy = "auditorium", cascade = CascadeType.REMOVE)
    @XmlTransient
    private Collection<ScheduleEntry> scheduleEntries;

}
