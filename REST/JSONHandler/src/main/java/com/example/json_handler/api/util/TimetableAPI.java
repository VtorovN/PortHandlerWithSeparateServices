package com.example.json_handler.api.util;

import com.example.json_handler.lib.models.Vessel;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface TimetableAPI {
    @GET("generator")
    Call<List<Vessel>> getTimetable();
}
