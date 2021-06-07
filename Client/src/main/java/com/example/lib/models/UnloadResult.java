package com.example.lib.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class UnloadResult {
    @JsonInclude
    private final String vesselName;
    @JsonInclude
    private final String arrivalDate;
    @JsonInclude
    private String unloadStartDate;
    @JsonInclude
    private int unloadTimeInMinutes;

    @JsonIgnore
    private final long arrivalDateInMinutes;
    @JsonInclude
    private long unloadStartInMinutes;

    public UnloadResult(String vesselName, GregorianCalendar arrivalDate, long arrivalDateInMinutes) {
        this.vesselName = vesselName;

        SimpleDateFormat format = new SimpleDateFormat("dd:HH:mm");
        this.arrivalDate = format.format(arrivalDate.getTime());

        this.arrivalDateInMinutes = arrivalDateInMinutes;

        unloadTimeInMinutes = -1;
    }

    public String getVesselName() {
        return vesselName;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public String getUnloadStartDate() {
        return unloadStartDate;
    }

    public int getIdleTimeInMinutes() {
        return (int) (unloadStartInMinutes - arrivalDateInMinutes);
    }

    public int getUnloadTimeInMinutes() {
        return unloadTimeInMinutes;
    }

    public void setUnloadStartInMinutes(long unloadStartInMinutes) {
        this.unloadStartInMinutes = unloadStartInMinutes;

        GregorianCalendar currentCalendar = new GregorianCalendar(2021, Calendar.MARCH, 1, 0, 0);
        currentCalendar.add(Calendar.MINUTE, (int) unloadStartInMinutes);

        SimpleDateFormat format = new SimpleDateFormat("dd:HH:mm");
        unloadStartDate = format.format(currentCalendar.getTime());
    }

    public void finalizeUnloadTime(long currentDate) {
        unloadTimeInMinutes = (int) (currentDate - unloadStartInMinutes);
    }
}
