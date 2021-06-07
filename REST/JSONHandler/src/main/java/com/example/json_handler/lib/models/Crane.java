package com.example.json_handler.lib.models;

public class Crane implements Runnable {
    public final Object vesselAssignmentNotifier = new Object();
    private final CargoType handledCargoType;
    private final int productivityPerMinute;
    private Vessel assignedVessel;
    private boolean isWorking;

    public Crane(CargoType handledCargoType, int productivityPerMinute) {
        this.handledCargoType = handledCargoType;
        this.productivityPerMinute = productivityPerMinute;
        assignedVessel = null;
    }

    public CargoType getHandledCargoType() {
        return handledCargoType;
    }

    public int getProductivityPerMinute() {
        return productivityPerMinute;
    }

    public Vessel getAssignedVessel() {
        return assignedVessel;
    }

    public boolean isBusy() {
        return assignedVessel != null;
    }

    public synchronized void assignVessel(Vessel vessel) throws RuntimeException {
        if (vessel != null) {
            if (vessel.getCargo().getType() != handledCargoType) {
                throw new IllegalArgumentException("Crane with handledCargoType " + handledCargoType
                        + " cannot handle vessel with Cargo of type " + vessel.getCargo().getType());
            }

            assignedVessel = vessel;

            vessel.assignCrane();
        }
    }

    public synchronized void unassignVessel() {
        if (isBusy()) {
            assignedVessel.unassignCrane();
            assignedVessel = null;
        }
    }

    public void endWork() {
        isWorking = false;
    }

    @Override
    public void run() {
        isWorking = true;
        while (isWorking) {
            if (!isBusy()) {
                System.out.println("Crane " + handledCargoType + " waiting...");
                try {
                    synchronized (vesselAssignmentNotifier) {
                        vesselAssignmentNotifier.wait();
                    }
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                System.out.println("Crane " + handledCargoType + " woke up");
            }
            if (isBusy()) {
                Cargo cargo = assignedVessel.getCargo();

                if (!cargo.isEmpty()) {
                    cargo.decreaseCurrentAmount(productivityPerMinute);
                }
            }
        }
    }
}
