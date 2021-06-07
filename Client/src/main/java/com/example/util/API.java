package com.example.util;

import com.example.lib.models.Vessel;

import com.example.lib.models.SimulationResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface API {
    @GET("timetable")
    Call<List<Vessel>> getTimetable(@Query("filename") String filename);

    @POST("save-result")
    Call<Void> saveResult(@Body SimulationResult result, @Query("filename") String filename);
}
