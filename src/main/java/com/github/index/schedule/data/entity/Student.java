package com.github.index.schedule.data.entity;


import com.github.index.schedule.adapters.LocalDateAdapter;
import com.github.index.schedule.converters.LocalDateAttributeConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.oxm.annotations.XmlInverseReference;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Student implements Serializable {
    @Id
    @Column(name = "student_id")
    private int studentId;

    @Size(min = 2, max = 32)
    @Column(name = "first_name", length = 32, nullable = false)
    private String firstName;

    @Size(min = 2, max = 32)
    @Column(name = "last_name", length = 32, nullable = false)
    private String lastName;

    @Size(min = 2, max = 32)
    @Column(name = "patronymic", length = 32, nullable = false)
    private String patronymic;

    @Column(name = "birth_day", nullable = false)
    @Convert(converter = LocalDateAttributeConverter.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDay;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "group_id")
    @XmlInverseReference(mappedBy = "students")
    private Group group;
}

