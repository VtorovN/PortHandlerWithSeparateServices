package com.example.json_handler.lib.models;

import com.example.json_handler.api.util.UnloadResultDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

@JsonDeserialize(using = UnloadResultDeserializer.class)
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
    private long arrivalDateInMinutes;

    @JsonIgnore
    private long unloadStartInMinutes;

    public UnloadResult(String vesselName, String arrivalDate, String unloadStartDate, int unloadTimeInMinutes) {
        this.vesselName = vesselName;
        this.arrivalDate = arrivalDate;
        this.unloadStartDate = unloadStartDate;
        this.unloadTimeInMinutes = unloadTimeInMinutes;
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
