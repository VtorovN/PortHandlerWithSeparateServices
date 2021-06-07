package com.example.json_handler.lib.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public class SimulationResult {
    @JsonInclude
    private final List<UnloadResult> unloadResults;

    @JsonInclude
    private final int fineSummary;
    @JsonInclude
    private final int unloadedVesselsCount;
    @JsonInclude
    private final int maxUnloadDelay;

    @JsonInclude
    private final float averageUnloadDelay;
    @JsonInclude
    private final float averageUnloadQueueLength;
    @JsonInclude
    private final float averageTimeInQueueInMinutes;

    private final Map<CargoType, Integer> estimatedCranesCount;

    public SimulationResult(List<UnloadResult> unloadResults, int fineSummary, int unloadedVesselsCount,
                            int maxUnloadDelay, float averageUnloadDelay, float averageUnloadQueueLength,
                            float averageTimeInQueueInMinutes, Map<CargoType, Integer> estimatedCranesCount) {
        this.unloadResults = unloadResults;
        this.fineSummary = fineSummary;
        this.unloadedVesselsCount = unloadedVesselsCount;
        this.maxUnloadDelay = maxUnloadDelay;
        this.averageUnloadDelay = averageUnloadDelay;
        this.averageUnloadQueueLength = averageUnloadQueueLength;
        this.averageTimeInQueueInMinutes = averageTimeInQueueInMinutes;
        this.estimatedCranesCount = estimatedCranesCount;
    }

    public List<UnloadResult> getUnloadResults() {
        return unloadResults;
    }

    public int getFineSummary() {
        return fineSummary;
    }

    public int getUnloadedVesselsCount() {
        return unloadedVesselsCount;
    }

    public int getMaxUnloadDelay() {
        return maxUnloadDelay;
    }

    public float getAverageUnloadDelay() {
        return averageUnloadDelay;
    }

    public float getAverageUnloadQueueLength() {
        return averageUnloadQueueLength;
    }

    public float getAverageTimeInQueueInMinutes() {
        return averageTimeInQueueInMinutes;
    }

    public Map<CargoType, Integer> getEstimatedCranesCount() {
        return estimatedCranesCount;
    }
}
