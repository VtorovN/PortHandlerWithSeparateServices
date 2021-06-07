package com.example.lib.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
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

    private final float averageUnloadDelay;
    @JsonInclude
    private final float averageUnloadQueueLength;
    @JsonInclude
    private final float averageTimeInQueueInMinutes;

    private final Map<CargoType, Integer> estimatedCranesCount;

    public SimulationResult(List<UnloadResult> unloadResults, int fineSummary, int maxUnloadDelay,
                            float averageUnloadDelay, float averageUnloadQueueLength, float averageTimeInQueueInMinutes,
                            List<Crane> craneList, Map<CargoType, Integer> finesByCargoType) {
        this.unloadResults = unloadResults;
        this.fineSummary = fineSummary;
        this.unloadedVesselsCount = unloadResults.size();
        this.maxUnloadDelay = maxUnloadDelay;
        this.averageUnloadDelay = averageUnloadDelay;
        this.averageUnloadQueueLength = averageUnloadQueueLength;
        this.averageTimeInQueueInMinutes = averageTimeInQueueInMinutes;

        estimatedCranesCount = new HashMap<>();

        estimatedCranesCount.put(CargoType.DRY, 0);
        estimatedCranesCount.put(CargoType.LIQUID, 0);
        estimatedCranesCount.put(CargoType.CONTAINER, 0);

        for (Crane crane : craneList) {
            int count = estimatedCranesCount.get(crane.getHandledCargoType());
            count++;
            estimatedCranesCount.put(crane.getHandledCargoType(), count);
        }

        final int craneCost = 30000;

        // Temporary values to estimate needed cranes count
        int dryCargoFine = finesByCargoType.get(CargoType.DRY);
        int liquidCargoFine = finesByCargoType.get(CargoType.LIQUID);
        int containerCargoFine = finesByCargoType.get(CargoType.CONTAINER);

        while (fineSummary >= craneCost) {
            fineSummary -= craneCost;

            int maxFine = Math.max(dryCargoFine, Math.max(liquidCargoFine, containerCargoFine));

            if (maxFine == dryCargoFine) {
                dryCargoFine -= craneCost;

                int count = estimatedCranesCount.get(CargoType.DRY);
                count++;
                estimatedCranesCount.put(CargoType.DRY, count);
            }
            else if (maxFine == liquidCargoFine) {
                liquidCargoFine -= craneCost;

                int count = estimatedCranesCount.get(CargoType.LIQUID);
                count++;
                estimatedCranesCount.put(CargoType.LIQUID, count);

            }
            else {
                containerCargoFine -= craneCost;

                int count = estimatedCranesCount.get(CargoType.CONTAINER);
                count++;
                estimatedCranesCount.put(CargoType.CONTAINER, count);
            }
        }
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
