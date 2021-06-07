package com.example.json_handler.api.util;

import com.example.json_handler.lib.models.UnloadResult;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class UnloadResultDeserializer extends StdDeserializer<UnloadResult> {
    public UnloadResultDeserializer() {
        this(null);
    }

    public UnloadResultDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public UnloadResult deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String vesselName = node.get("vesselName").asText();
        String arrivalDate = node.get("arrivalDate").asText();
        String unloadStartDate = node.get("unloadStartDate").asText();

        int unloadTimeInMinutes = node.get("unloadTimeInMinutes").asInt();

        return new UnloadResult(vesselName, arrivalDate, unloadStartDate, unloadTimeInMinutes);
    }
}