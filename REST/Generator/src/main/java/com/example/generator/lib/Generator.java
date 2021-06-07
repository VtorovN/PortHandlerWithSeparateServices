package com.example.generator.lib;

import com.example.generator.lib.models.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class Generator {
    private static final int UPPER_BOUND = 10000;
    private static final int LOWER_BOUND = 1000;
    private static final int STARTING_YEAR = 2021;
    private static final int STARTING_MONTH = Calendar.MARCH;
    private static final int STARTING_DAY = 1;
    private static final int ENDING_DAY = 30;
    private static final int STARTING_HOUR = 0;
    private static final int ENDING_HOUR = 23;
    private static final Random rand = new Random();
    private static final List<CargoType> cargoTypes = Arrays.asList(CargoType.values());

    public static Vessel generateVessel() {
        CargoType cargoType = cargoTypes.get(rand.nextInt(cargoTypes.size()));
        int cargoAmount = rand.nextInt(UPPER_BOUND - LOWER_BOUND + 1) + LOWER_BOUND;
        Cargo cargo = new Cargo(cargoType, cargoAmount);

        int arrivalDay = rand.nextInt(ENDING_DAY - STARTING_DAY + 1) + STARTING_DAY;
        int arrivalHour = rand.nextInt(ENDING_HOUR - STARTING_HOUR + 1) + STARTING_HOUR;
        GregorianCalendar calendar = 
                new GregorianCalendar(STARTING_YEAR, STARTING_MONTH, arrivalDay, arrivalHour, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyykkmmssSS");
        String vesselNumber = dateFormat.format(GregorianCalendar.getInstance().getTime());
        String name = "Vessel " + vesselNumber;

        return new Vessel(name, cargo, calendar);
    }
}
