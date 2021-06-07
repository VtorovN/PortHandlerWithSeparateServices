package com.example.json_handler.api;

import com.example.json_handler.api.util.TimetableAPI;
import com.example.json_handler.lib.models.Vessel;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;

public class RetrofitHandler {
    private static Retrofit retrofit;
    private static TimetableAPI timetableAPI;

    static {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://130.61.185.88:8080/api/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        timetableAPI = retrofit.create(TimetableAPI.class);
    }

    public static List<Vessel> getTimetable() throws IOException {
        Call<List<Vessel>> timetableMessage = timetableAPI.getTimetable();

        return timetableMessage.execute().body();
    }
}
