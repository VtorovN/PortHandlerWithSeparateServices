package com.example.lib.models;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Cargo {
    @JsonInclude
    private final CargoType type;

    @JsonInclude
    private final int initialAmount;

    @JsonInclude
    private int currentAmount;

    public Cargo(CargoType type, int initialAmount) {
        this.type = type;
        this.initialAmount = initialAmount;
        currentAmount = initialAmount;
    }

    public Cargo(CargoType type, int initialAmount, int currentAmount) {
        this.type = type;
        this.initialAmount = initialAmount;
        this.currentAmount = currentAmount;
    }

    public CargoType getType() {
        return type;
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public boolean isEmpty() {
        return currentAmount == 0;
    }

    public synchronized void decreaseCurrentAmount(int decreaseAmount) throws RuntimeException {
        if (decreaseAmount <= 0) {
            throw new IllegalArgumentException("Cargo decrease amount must be positive");
        }
        else if (isEmpty()) {
            throw new RuntimeException("Couldn't decrease current cargo amount: cargo is empty");
        }

        currentAmount -= decreaseAmount;

        if (currentAmount < 0) {
            currentAmount = 0;
        }
    }
}
