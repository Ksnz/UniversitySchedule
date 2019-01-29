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

@Entity
@Table(name = "students")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Student extends Person {
    @Id
    @Column(name = "student_id")
    private int studentId;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "group_id")
    @XmlInverseReference(mappedBy = "students")
    private Group group;
}

