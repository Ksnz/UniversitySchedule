package com.github.index.schedule.data.entity;

import com.github.index.schedule.data.adapters.CharAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "faculties")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"groups"})
@ToString(exclude = {"groups"})
@XmlRootElement
public class Faculty implements Serializable {

    @Id
    private char id;

    @XmlJavaTypeAdapter(CharAdapter.class)
    public Character getId() {
        return id;
    }

    public void setId(Character id) {
        this.id = id;
    }

    @Column(name = "short_name", length = 8, nullable = false)
    private String shortName;

    @Column(name = "full_name", length = 64, nullable = false)
    private String fullName;

    @XmlTransient
    public List<Group> getGroups() {
        return groups;
    }

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.REMOVE)
    @XmlTransient
    List<Group> groups = new ArrayList<>();
}
