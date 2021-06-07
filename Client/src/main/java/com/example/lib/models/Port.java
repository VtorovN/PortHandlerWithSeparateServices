package com.example.lib.models;

import com.example.Simulator;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Port implements Runnable {
    public final Object vesselDepartureNotifier = new Object();

    private final int cranesProductivity;
    private final List<Crane> craneList;

    // Vessels awaiting for unload
    private final List<Vessel> waitingVessels;
    // Vessels with delayed departure
    private final List<AbstractMap.SimpleEntry<Vessel, Integer>> delayedDepartureVessels;
    // Vessels that will depart after unload delay
    private final List<AbstractMap.SimpleEntry<Vessel, Integer>> vesselsToDepartAfterDelay;

    private final AtomicInteger idleVesselsCount;
    private int nextDepartureSleepTime;
    private boolean isWorking;

    // Simulation variables
    Simulator simulator;

    private final AtomicInteger fine;
    private int maxUnloadDelay;
    private float averageUnloadDelay;
    private int maxUnloadQueueLength;
    private float averageUnloadQueueLength;
    private final List<UnloadResult> unloadedVesselResults;
    private final Map<CargoType, Integer> finesByCargoType;

    private final Map<Vessel, Integer> vesselWaitingTimeMap;

    public Port(int cranesProductivity, Simulator simulator) {
        this.cranesProductivity = cranesProductivity;
        this.simulator = simulator;

        craneList = new ArrayList<>();
        addCrane(CargoType.CONTAINER);
        addCrane(CargoType.DRY);
        addCrane(CargoType.LIQUID);

        waitingVessels = Collections.synchronizedList(new ArrayList<>());
        delayedDepartureVessels = Collections.synchronizedList(new ArrayList<>());
        vesselsToDepartAfterDelay = Collections.synchronizedList(new ArrayList<>());

        idleVesselsCount = new AtomicInteger(0);

        isWorking = true;

        nextDepartureSleepTime = 0;

        // Simulation block
        fine = new AtomicInteger(0);
        unloadedVesselResults = new ArrayList<>();
        finesByCargoType = new HashMap<>();

        finesByCargoType.put(CargoType.DRY, 0);
        finesByCargoType.put(CargoType.LIQUID, 0);
        finesByCargoType.put(CargoType.CONTAINER, 0);

        vesselWaitingTimeMap = new HashMap<>();
        // Simulation block end
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(craneList.size());

        for (Crane crane : craneList) {
            executor.submit(crane);
        }

        while (isWorking) {
            // Assign cranes to vessels of the same type
            for (Crane crane : craneList) {
                if (!crane.isBusy()) {
                    synchronized (waitingVessels) {
                        waitingVessels.stream()
                                .filter(vessel -> vessel.getCargo().getType() == crane.getHandledCargoType())
                                .findFirst().ifPresent(vessel -> {
                                    // Simulation block
                                    synchronized (unloadedVesselResults) {
                                        for (UnloadResult result : unloadedVesselResults) {
                                            if (result.getVesselName().equals(vessel.getName())) {
                                                result.setUnloadStartInMinutes(simulator.getCurrentTimeFromStart());
                                                break;
                                            }
                                        }
                                    }
                                    // Simulation block end

                                    try {
                                        crane.assignVessel(vessel);
                                    }
                                    catch (RuntimeException runtimeException) {
                                        runtimeException.printStackTrace();
                                        System.exit(1);
                                    }

                                    synchronized (crane.vesselAssignmentNotifier) {
                                        crane.vesselAssignmentNotifier.notify();
                                    }

                                    idleVesselsCount.decrementAndGet();
                                });
                    }
                }
            }

            delayedDepartureVessels.removeAll(vesselsToDepartAfterDelay);

            List<Vessel> departingVessels;
            // Synchronized as stream() is used. Stream uses spliterator(), which must be synchronized
            synchronized (vesselsToDepartAfterDelay) {
                // Mark vessels with 0 delay as departing
                departingVessels = vesselsToDepartAfterDelay.stream()
                        .map(AbstractMap.SimpleEntry::getKey)
                        .collect(Collectors.toList());

                vesselsToDepartAfterDelay.clear();
            }

            // Add unloaded vessels to delayed or departing
            Random rand = new Random();
            synchronized (waitingVessels) {
                for (Vessel vessel : waitingVessels) {
                    if (vessel.getCargo().isEmpty()) {
                        // random unload delay from 0 to 1440
                        int delay = rand.nextInt(1441);

                        // Simulation block
                        if (delay > maxUnloadDelay) {
                            maxUnloadDelay = delay;
                        }

                        if (averageUnloadDelay != 0) {
                            averageUnloadDelay += delay;
                            averageUnloadDelay /= 2;
                        }
                        else {
                            averageUnloadDelay = delay;
                        }
                        // Simulation block end

                        if (delay != 0) {
                            var delayedVesselPair = new AbstractMap.SimpleEntry<>(vessel, delay);

                            if (!delayedDepartureVessels.contains(delayedVesselPair)) {
                                delayedDepartureVessels.add(delayedVesselPair);

                                if (nextDepartureSleepTime == 0 || delay < nextDepartureSleepTime) {
                                    notifyTimeChange(nextDepartureSleepTime);
                                    nextDepartureSleepTime = delay;
                                    synchronized (vesselDepartureNotifier) {
                                        vesselDepartureNotifier.notify();
                                    }
                                }
                            }
                        }
                        else {
                            departingVessels.add(vessel);
                        }
                    }
                }
            }

            // Make vessels depart
            for (Crane crane : craneList) {
                if (departingVessels.contains(crane.getAssignedVessel())) {
                    crane.unassignVessel();
                }
            }

            if (departingVessels.size() != 0) {
                System.out.println("Vessels departed: " + departingVessels.size());
            }

            waitingVessels.removeAll(departingVessels);

            // Simulation block
            synchronized (unloadedVesselResults) {
                for (Vessel departingVessel : departingVessels) {
                    unloadedVesselResults.stream()
                            .filter(unloadResult -> unloadResult.getVesselName().equals(departingVessel.getName()))
                            .findFirst().ifPresent(unloadResult -> {
                                unloadResult.finalizeUnloadTime(simulator.getCurrentTimeFromStart());
                    });
                }
            }
            // Simulation block end
        }

        // Simulation block
        // Not all of the vessels are unloaded at the end of the simulation
        List<UnloadResult> resultsToRemove = new ArrayList<>();

        synchronized (unloadedVesselResults) {
            for (UnloadResult result : unloadedVesselResults) {
                synchronized (waitingVessels) {
                    for (Vessel vessel : waitingVessels) {
                        if (result.getVesselName().equals(vessel.getName())) {
                            resultsToRemove.add(result);
                            break;
                        }
                    }
                }
            }
        }

        unloadedVesselResults.removeAll(resultsToRemove);
        // Simulation block end

        for (Crane crane : craneList) {
            synchronized (crane.vesselAssignmentNotifier) {
                crane.vesselAssignmentNotifier.notify();
                crane.endWork();
            }
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                System.out.println("Cranes are not terminated");
            }
        }
        catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public void addCrane(CargoType cargoType) {
        craneList.add(new Crane(cargoType, cranesProductivity));
    }

    public void addArrivedVessel(Vessel arrivedVessel) {
        if (!waitingVessels.contains(arrivedVessel)) {
            idleVesselsCount.incrementAndGet();

            // Simulation block
            if (idleVesselsCount.get() >= maxUnloadQueueLength) {
                maxUnloadQueueLength = idleVesselsCount.get();
            }

            if (averageUnloadQueueLength != 0) {
                averageUnloadQueueLength += idleVesselsCount.get();
                averageUnloadQueueLength /= 2;
            }
            else {
                averageUnloadQueueLength = idleVesselsCount.get();
            }

            vesselWaitingTimeMap.put(arrivedVessel, 0);

            unloadedVesselResults.add(new UnloadResult(arrivedVessel.getName(),
                    arrivedVessel.getArrivalDate(), simulator.getCurrentTimeFromStart()));
            // Simulation block end

            waitingVessels.add(arrivedVessel);
        }
    }

    public int getNextDepartureSleepTime() {
        return nextDepartureSleepTime;
    }

    public void notifyTimeChange(int time) {
        nextDepartureSleepTime = 0;
        vesselWaitingTimeMap.values().forEach(waitTime -> waitTime += time);

        synchronized (delayedDepartureVessels) {
            for (var pair : delayedDepartureVessels) {
                pair.setValue(pair.getValue() - time);

                if (pair.getValue() <= 0) {
                    vesselsToDepartAfterDelay.add(pair);

                    // Simulation block
                    synchronized (unloadedVesselResults) {
                        for (UnloadResult result : unloadedVesselResults) {
                            if (result.getVesselName().equals(pair.getKey().getName())) {
                                result.finalizeUnloadTime(simulator.getCurrentTimeFromStart());
                                break;
                            }
                        }
                    }
                    //Simulation block end
                }
                else if (nextDepartureSleepTime == 0 || pair.getValue() < nextDepartureSleepTime) {
                    nextDepartureSleepTime = pair.getValue();
                }
            }
        }
    }

    public void notifyHourEnd() {
        final int fineByHour = 100;

        fine.addAndGet(idleVesselsCount.get() * fineByHour);

        // Simulation block
        synchronized (waitingVessels) {
            for (Vessel vessel : waitingVessels) {
                CargoType cargoType = vessel.getCargo().getType();
                synchronized (finesByCargoType) {
                    finesByCargoType.replace(cargoType, finesByCargoType.get(cargoType) + fineByHour);
                }
            }
        }
        //Simulation block end
    }

    public void endWork() {
        isWorking = false;
    }

    public boolean hasIdleVessels() {
        return idleVesselsCount.get() != 0;
    }

    public int getFine() {
        return fine.get();
    }

    public int getMaxUnloadDelay() {
        return maxUnloadDelay;
    }

    public float getAverageUnloadDelay() {
        return averageUnloadDelay;
    }

    public int getMaxUnloadQueueLength() {
        return maxUnloadQueueLength;
    }

    public float getAverageUnloadQueueLength() {
        return averageUnloadQueueLength;
    }

    public List<UnloadResult> getUnloadedVesselResults() {
        return unloadedVesselResults;
    }

    public List<Crane> getCraneList() {
        return craneList;
    }

    public Map<CargoType, Integer> getFinesByCargoType() {
        return finesByCargoType;
    }
}
