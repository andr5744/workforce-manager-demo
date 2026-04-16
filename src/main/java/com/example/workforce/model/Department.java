package com.example.workforce.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String location;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal budget;

    @Column(name = "head_count_limit")
    private Integer headCountLimit;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    public Department() {}

    public Department(String name, String location, BigDecimal budget, Integer headCountLimit) {
        this.name = name;
        this.location = location;
        this.budget = budget;
        this.headCountLimit = headCountLimit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public Integer getHeadCountLimit() { return headCountLimit; }
    public void setHeadCountLimit(Integer headCountLimit) { this.headCountLimit = headCountLimit; }

    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
}
