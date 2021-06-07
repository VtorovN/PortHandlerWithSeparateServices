package com.example;

import com.example.lib.models.Port;
import com.example.lib.models.SimulationResult;
import com.example.lib.models.UnloadResult;
import com.example.lib.models.Vessel;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Simulator {
    private Port handledPort;

    private long startTime;
    private long currentTime;
    private long endTime;
    private long vesselArrivalSleepTime;
    private long nextVesselArrivalTime;
    private long hourEndSleepTime;

    private List<Vessel> vesselsToArrive;

    private SimulationResult latestSimulationResult;

    public Simulator(List<Vessel> vesselList) {
        vesselsToArrive = vesselList;
    }

    private void init() throws IOException {
        handledPort = new Port(1, this);

        GregorianCalendar currentCalendar = new GregorianCalendar(2021, Calendar.MARCH, 1, 0, 0);

        startTime = (int) (currentCalendar.getTime().getTime() / 1000 / 60);
        currentTime = startTime;
        endTime = currentTime + 30 * 24 * 60;
        vesselArrivalSleepTime = 0;
        nextVesselArrivalTime = 0;
        hourEndSleepTime = 60;

        Random random = new Random();

        // 10080 minutes == 7 days
        final int min = -10080;
        final int max = -min;

        // Changing arrival date of each vessel, adding or subtracting delay in minutes
        for (Vessel vessel : vesselsToArrive) {
            int delay = random.nextInt(max - min) + min;
            vessel.getArrivalDate().add(Calendar.MINUTE, delay);
        }

        vesselsToArrive.sort(Comparator.comparing(Vessel::getArrivalDate));

        // Delete all that missed 1 march
        while (!vesselsToArrive.isEmpty() && vesselsToArrive.get(0).getArrivalDate().before(currentCalendar)) {
            vesselsToArrive.remove(0);
        }
    }

    public long getCurrentTimeFromStart() {
        return currentTime - startTime;
    }

    public SimulationResult getLatestSimulationResult() {
        return latestSimulationResult;
    }

    public void simulate() {
        try {
            init();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> handledPortFuture = executor.submit(handledPort);

        Vessel nextVessel = null;

        while (endTime > currentTime) {
            System.out.println("Time left: " + (endTime - currentTime));
            if (vesselArrivalSleepTime == 0 && !vesselsToArrive.isEmpty()) {
                nextVessel = vesselsToArrive.get(0);
            }

            if (nextVessel != null) {
                if (vesselArrivalSleepTime == 0) {
                    nextVesselArrivalTime = nextVessel.getArrivalDate().getTime().getTime() / 1000 / 60;
                    vesselArrivalSleepTime = nextVesselArrivalTime - currentTime;
                }

                // if vessel is arriving earlier
                if ((handledPort.getNextDepartureSleepTime() == 0 || vesselArrivalSleepTime < handledPort.getNextDepartureSleepTime())
                        && (!handledPort.hasIdleVessels() || vesselArrivalSleepTime < hourEndSleepTime)) {
                    try {
                        if (currentTime + vesselArrivalSleepTime >= endTime) {
                            break;
                        }

                        System.out.println("Simulator waiting for arriving vessel, time - " + vesselArrivalSleepTime);

                        int elapsedTime = 0;

                        // Sometimes threads work too fast so that vessel arrival is skipped
                        if (vesselArrivalSleepTime > 0) {

                            long start = System.currentTimeMillis();

                            synchronized (handledPort.vesselDepartureNotifier) {
                                handledPort.vesselDepartureNotifier.wait(vesselArrivalSleepTime);
                            }

                            long end = System.currentTimeMillis();
                            elapsedTime = (int) (end - start);
                        }

                        if (elapsedTime != 0) {
                            hourEndSleepTime -= elapsedTime % 60;
                            if (hourEndSleepTime <= 0) {
                                hourEndSleepTime += 60;
                                handledPort.notifyHourEnd();
                            }

                            currentTime += elapsedTime;

                            // handledPort.vesselDepartureNotifier worked
                            if (elapsedTime < vesselArrivalSleepTime) {
                                if (vesselArrivalSleepTime != 0) {
                                    vesselArrivalSleepTime -= elapsedTime;

                                    if (vesselArrivalSleepTime <= 0) {
                                        handledPort.addArrivedVessel(nextVessel);
                                        vesselsToArrive.remove(nextVessel);
                                        nextVessel = null;
                                        vesselArrivalSleepTime = 0;
                                    }
                                }
                                continue;
                            }
                        }

                        handledPort.addArrivedVessel(nextVessel);
                        vesselsToArrive.remove(nextVessel);
                        nextVessel = null;
                        vesselArrivalSleepTime = 0;

                        continue;
                    }
                    catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            // if vessel is departing earlier
            if (handledPort.getNextDepartureSleepTime() != 0
                    && (!handledPort.hasIdleVessels() || handledPort.getNextDepartureSleepTime() < hourEndSleepTime)) {
                try {
                    if (currentTime + handledPort.getNextDepartureSleepTime() >= endTime) {
                        break;
                    }

                    int oldNextDepartureTime = handledPort.getNextDepartureSleepTime();

                    System.out.println("Simulator waiting for departing vessel, time - " + handledPort.getNextDepartureSleepTime());

                    long start = System.currentTimeMillis();

                    synchronized (handledPort.vesselDepartureNotifier) {
                        handledPort.vesselDepartureNotifier.wait(handledPort.getNextDepartureSleepTime());
                    }

                    long end = System.currentTimeMillis();
                    int elapsedTime = (int) (end - start);

                    hourEndSleepTime -= elapsedTime % 60;
                    if (hourEndSleepTime <= 0) {
                        hourEndSleepTime += 60;
                        handledPort.notifyHourEnd();
                    }

                    currentTime += elapsedTime;

                    if (vesselArrivalSleepTime != 0) {
                        vesselArrivalSleepTime -= elapsedTime;

                        if (vesselArrivalSleepTime <= 0) {
                            handledPort.addArrivedVessel(nextVessel);
                            vesselsToArrive.remove(nextVessel);
                            nextVessel = null;
                            vesselArrivalSleepTime = 0;
                        }
                    }

                    handledPort.notifyTimeChange(oldNextDepartureTime);
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    System.exit(1);
                }
            }

            if (!handledPort.hasIdleVessels()
                    && handledPort.getNextDepartureSleepTime() == 0 && vesselArrivalSleepTime == 0) {
                try {
                    long sleepTime = endTime - currentTime;

                    long start = System.currentTimeMillis();

                    System.out.println("Simulator waiting for simulation end, time - " + sleepTime);

                    synchronized (handledPort.vesselDepartureNotifier) {
                        handledPort.vesselDepartureNotifier.wait(sleepTime);
                    }

                    long end = System.currentTimeMillis();
                    int elapsedTime = (int) (end - start);

                    hourEndSleepTime -= elapsedTime % 60;
                    if (hourEndSleepTime <= 0) {
                        hourEndSleepTime += 60;
                        handledPort.notifyHourEnd();
                    }

                    currentTime += elapsedTime;
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    System.exit(1);
                }
            }
            else {
                // if hour ends earlier or vessel arrives or departs at hour end
                while (handledPort.hasIdleVessels()
                        || vesselArrivalSleepTime == hourEndSleepTime
                        || handledPort.getNextDepartureSleepTime() == hourEndSleepTime) {
                    try {
                        if (vesselArrivalSleepTime != 0 && vesselArrivalSleepTime < hourEndSleepTime
                                || handledPort.getNextDepartureSleepTime() != 0
                                && handledPort.getNextDepartureSleepTime() < hourEndSleepTime) {
                            break;
                        }

                        if (currentTime + hourEndSleepTime > endTime) {
                            break;
                        }

                        //System.out.println("Simulator waiting for hour end, time - " + hourEndSleepTime);

                        long start = System.currentTimeMillis();

                        synchronized (handledPort.vesselDepartureNotifier) {
                            handledPort.vesselDepartureNotifier.wait(hourEndSleepTime);
                        }

                        long end = System.currentTimeMillis();
                        int elapsedTime = (int) (end - start);

                        hourEndSleepTime -= elapsedTime % 60;
                        if (hourEndSleepTime <= 0) {
                            hourEndSleepTime += 60;
                            handledPort.notifyHourEnd();
                        }
                        if (vesselArrivalSleepTime != 0) {
                            vesselArrivalSleepTime -= elapsedTime;

                            if (vesselArrivalSleepTime <= 0) {
                                handledPort.addArrivedVessel(nextVessel);
                                vesselsToArrive.remove(nextVessel);
                                nextVessel = null;
                                vesselArrivalSleepTime = 0;
                            }
                        }
                        currentTime += elapsedTime;

                        // handledPort.vesselDepartureNotifier worked
                        if (elapsedTime < hourEndSleepTime) {
                            break;
                        }

                        handledPort.notifyHourEnd();

                        if (handledPort.getNextDepartureSleepTime() == hourEndSleepTime
                                || vesselArrivalSleepTime == hourEndSleepTime) {

                            if (vesselArrivalSleepTime == hourEndSleepTime) {
                                handledPort.addArrivedVessel(nextVessel);
                                vesselsToArrive.remove(nextVessel);
                                nextVessel = null;
                                vesselArrivalSleepTime = 0;
                            }

                            if (handledPort.getNextDepartureSleepTime() == hourEndSleepTime) {
                                handledPort.notifyTimeChange(handledPort.getNextDepartureSleepTime());
                            }
                        }
                    }
                    catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }

        handledPort.endWork();
        handledPortFuture.cancel(false);
        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        float averageTimeInQueue = 0;

        for (UnloadResult result : handledPort.getUnloadedVesselResults()) {
            if (result.getIdleTimeInMinutes() >= 0) {
                averageTimeInQueue += result.getIdleTimeInMinutes();
            }
        }

        averageTimeInQueue /= handledPort.getUnloadedVesselResults().size();

        latestSimulationResult = new SimulationResult(
                handledPort.getUnloadedVesselResults(),
                handledPort.getFine(),
                handledPort.getMaxUnloadDelay(),
                handledPort.getAverageUnloadDelay(),
                handledPort.getAverageUnloadQueueLength(),
                averageTimeInQueue,
                handledPort.getCraneList(),
                handledPort.getFinesByCargoType()
        );
    }
}
