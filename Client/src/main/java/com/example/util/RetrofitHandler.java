package com.example.util;

import com.example.lib.models.SimulationResult;
import com.example.lib.models.Vessel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;

public class RetrofitHandler {
    private static Retrofit retrofit;
    private static API api;

    static {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://130.61.185.88:8008/api/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        api = retrofit.create(API.class);
    }

    public static List<Vessel> getTimetable(String filename) throws IOException {
        Call<List<Vessel>> timetableMessage = api.getTimetable(filename);

        List<Vessel> vesselList = timetableMessage.execute().body();

        return vesselList;
    }

    public static void saveResult(SimulationResult result, String filename) throws IllegalArgumentException, IOException {
        if (filename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        api.saveResult(result, filename).execute();
    }
}
