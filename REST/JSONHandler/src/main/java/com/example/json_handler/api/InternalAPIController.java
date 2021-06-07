package com.example.json_handler.api;

import com.example.json_handler.lib.models.SimulationResult;
import com.example.json_handler.lib.models.Vessel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
public class InternalAPIController {

	@GetMapping("/api/timetable")
	public ResponseEntity<Object> getTimetable(@RequestParam(value = "filename", defaultValue = "") String filename) {
		List<Vessel> vesselList;

		if (filename.isEmpty()) {
			try {
				vesselList = RetrofitHandler.getTimetable();
			}
			catch (IOException ioException) {
				ioException.printStackTrace();
				return ResponseEntity
						.status(HttpStatus.BAD_GATEWAY)
						.body("Couldn't get timetable from the external server");
			}
		}
		else {
			ObjectMapper objectMapper = new ObjectMapper();

			try {
				vesselList = Arrays.asList(
						objectMapper.readValue(
								Paths.get("/home/ubuntu/port/timetables/" + filename).toFile(),
								Vessel[].class
						)
				);
			}
			catch (IOException ioException) {
				ioException.printStackTrace();
				return ResponseEntity
						.status(HttpStatus.NOT_FOUND)
						.body("Timetable with name \""  + filename + "\" not found");
			}
		}

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(vesselList);
	}

	@PostMapping("/api/save-result")
	public void saveResult(@RequestBody SimulationResult result, @RequestParam String filename) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			File file = new File("/home/ubuntu/port/results/" + filename);
			file.createNewFile();
			objectMapper.writer().writeValue(file, result);
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
