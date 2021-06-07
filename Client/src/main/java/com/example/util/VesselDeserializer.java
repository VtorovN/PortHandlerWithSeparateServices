package com.example.util;

import com.example.lib.models.Cargo;
import com.example.lib.models.CargoType;
import com.example.lib.models.Vessel;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class VesselDeserializer extends StdDeserializer<Vessel> {
    public VesselDeserializer() {
        this(null);
    }

    public VesselDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Vessel deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();

        CargoType cargoType = CargoType.valueOf(node.get("cargo").get("type").asText());
        int cargoInitialAmount = node.get("cargo").get("initialAmount").intValue();
        int cargoCurrentAmount = node.get("cargo").get("currentAmount").intValue();

        Cargo cargo = new Cargo(cargoType, cargoInitialAmount, cargoCurrentAmount);

        String arrivalDateString = node.get("arrivalDate").asText();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        GregorianCalendar arrivalDate = new GregorianCalendar();
        Date date = new Date();

        try {
            date = df.parse(arrivalDateString);
        }
        catch (ParseException parseException) {
            parseException.printStackTrace();
        }

        arrivalDate.setTime(date);

        return new Vessel(name, cargo, arrivalDate);
    }
}
