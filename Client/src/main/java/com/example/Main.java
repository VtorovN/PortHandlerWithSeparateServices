package com.example;

import com.example.lib.models.Vessel;
import com.example.util.RetrofitHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.print("Input filename to get from server: ");
        Scanner scanner = new Scanner(System.in);
        String filename = scanner.nextLine();

        List<Vessel> vesselList = new ArrayList<>();

        try {
            vesselList = RetrofitHandler.getTimetable(filename);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1);
        }

        if (vesselList != null) {
            System.out.println("Got List<Vessel> of size " + vesselList.size());
        }

        System.out.println("Starting simulation");
        System.out.println();
        Simulator simulator = new Simulator(vesselList);
        simulator.simulate();

        try {
            RetrofitHandler.saveResult(simulator.getLatestSimulationResult(), "savedResult");
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1);
        }
    }
}
