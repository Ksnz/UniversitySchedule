package com.github.index.schedule.data.entity;

import com.github.index.schedule.data.adapters.LocalTimeAdapter;
import com.github.index.schedule.data.converters.LocalTimeAttributeConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "schedule_entries")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"groups"})
@ToString(exclude = {"groups"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduleEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(nullable = false)
    Lecturer lecturer;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "room", referencedColumnName = "room", nullable = false),
            @JoinColumn(name = "housing", referencedColumnName = "housing", nullable = false)})
    Auditorium auditorium;

    @ManyToOne
    @JoinColumn(nullable = false)
    Course course;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "schedule_group",
            joinColumns = @JoinColumn(name = "schedule_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "group_id", nullable = false))
    Set<Group> groups = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    DayOfWeek dayOfWeek;

    @Column(name = "week_number", nullable = false)
    byte weekNumber;

    @Column(name = "start_time", nullable = false)
    @Convert(converter = LocalTimeAttributeConverter.class)
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @Convert(converter = LocalTimeAttributeConverter.class)
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    LocalTime endTime;
}
