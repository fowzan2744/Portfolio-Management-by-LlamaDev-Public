package com.backend.Portfolio_Backend.dto;

public class AllocationDTO {
    private String name;
    private Double value;
    private String fullName;

    public AllocationDTO() {
    }

    public AllocationDTO(String name, Double value, String fullName) {
        this.name = name;
        this.value = value;
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
