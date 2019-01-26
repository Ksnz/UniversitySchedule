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
@Table(name = "courses")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"scheduleEntries"})
@ToString(exclude = {"scheduleEntries"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Course implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "short_name", length = 8, nullable = false)
    private String shortName;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    @XmlTransient
    private Collection<ScheduleEntry> scheduleEntries;
}
