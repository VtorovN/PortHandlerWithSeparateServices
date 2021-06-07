package com.example.generator.lib.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.GregorianCalendar;

public class Vessel {
    private final String name;
    private final Cargo cargo;
    private final GregorianCalendar arrivalDate;

    @JsonIgnore
    private int assignedCranesCount;

    public Vessel(String name, Cargo cargo, GregorianCalendar arrivalDate) {
        this.name = name;
        this.cargo = cargo;
        this.arrivalDate = arrivalDate;
        assignedCranesCount = 0;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public GregorianCalendar getArrivalDate() {
        return arrivalDate;
    }

    public String getName() {
        return name;
    }

    public int getAssignedCranesCount() {
        return assignedCranesCount;
    }

    public synchronized void assignCrane() throws RuntimeException {
        if (assignedCranesCount == 2) {
            throw new RuntimeException("Crane assignment failed: there are already 2 cranes assigned");
        }
        assignedCranesCount++;
    }

    public synchronized void unassignCrane() throws RuntimeException {
        if (assignedCranesCount == 0) {
            throw new RuntimeException("Crane unassignment failed: there are 0 cranes assigned");
        }
        assignedCranesCount--;
    }
}
