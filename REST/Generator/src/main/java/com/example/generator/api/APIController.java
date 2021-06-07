package com.example.generator.api;

import com.example.generator.lib.models.*;

import com.example.generator.lib.Generator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class APIController {

	@GetMapping("/api/generator")
	public List<Vessel> generator() {
		List<Vessel> vesselList = new ArrayList<>();

		for (int i = 0; i < 30; i++) {
			vesselList.add(Generator.generateVessel());
		}

		return vesselList;
	}
}
