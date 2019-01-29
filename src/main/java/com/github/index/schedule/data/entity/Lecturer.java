package com.github.index.schedule.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

@Entity
@Table(name = "lecturers")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"scheduleEntries"}, callSuper = true)
@ToString(exclude = {"scheduleEntries"}, callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Lecturer extends Person {
    @Id
    @TableGenerator(name = "Lecturer_Gen", initialValue = 500000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Lecturer_Gen")
    @Column(name = "lecturer_id")
    private int lecturerId;

    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @XmlTransient
    private Collection<ScheduleEntry> scheduleEntries;
}
