package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blood_group")
@Getter
@Setter
@NoArgsConstructor
public class BloodGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_group", nullable = false, unique = true, length = 5)
    private String bloodGroup;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "bloodGroup")
    private List<Donor> donors = new ArrayList<>();

    public BloodGroup(String bloodGroup, String description) {
        this.bloodGroup = bloodGroup;
        this.description = description;
    }
}