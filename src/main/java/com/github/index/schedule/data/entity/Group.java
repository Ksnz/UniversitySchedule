package com.github.index.schedule.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.oxm.annotations.XmlInverseReference;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"scheduleEntries", "students"})
@ToString(exclude = {"scheduleEntries", "students"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Group implements Serializable {

    @Id
    @Column(name = "group_id")
    private int groupId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @XmlTransient
    private List<Student> students = new ArrayList<>(30);

    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(nullable = false)
    @XmlInverseReference(mappedBy = "groups")
    private Faculty faculty;

    @ManyToMany(mappedBy = "groups", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @XmlTransient
    private Set<ScheduleEntry> scheduleEntries = new HashSet<>();
}
