package com.github.index.schedule.data.entity;

import com.github.index.schedule.adapters.LocalDateAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;

@Entity
@Table(name = "lecturers")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"scheduleEntries"})
@ToString(exclude = {"scheduleEntries"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Lecturer implements Serializable {
    @Id
    @TableGenerator(name = "Lecturer_Gen", initialValue = 500000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Lecturer_Gen")
    @Column(name = "lecturer_id")
    private int lecturerId;

    @Size(min = 2, max = 32)
    @Column(name = "first_name", length = 32, nullable = false)
    private String firstName;

    @Size(min = 2, max = 32)
    @Column(name = "last_name", length = 32, nullable = false)
    private String lastName;

    @Size(min = 2, max = 32)
    @Column(length = 32, nullable = false)
    private String patronymic;

    @Column(name = "birth_day", nullable = false)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDay;

    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @XmlTransient
    private Collection<ScheduleEntry> scheduleEntries;
}
