package com.json.demo.jsondemo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JsonSettings {

    private ObjectMapper objectMapper;

    @PostConstruct
    void postConstruct() {
        objectMapper = new ObjectMapper();
        SimpleModule mapModule = new SimpleModule();
        MapDeserializer mapDeserializer = new MapDeserializer(Map.class);
        mapModule.addDeserializer(Map.class, mapDeserializer);
        mapModule.addSerializer(new MapSerialize());
        objectMapper.registerModule(mapModule);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static class MapDeserializer extends StdDeserializer<Map> {
        protected MapDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Map deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            
            LinkedHashMap map = new LinkedHashMap();
            node.fields().forEachRemaining(stringJsonNodeEntry -> map.put(rewriteKey(stringJsonNodeEntry.getKey()), parseObject(stringJsonNodeEntry.getValue())));
            return map;
        }

        private String rewriteKey(String key) {
            return key+"===";
        }

        private Object parseObject(JsonNode value) {
            if (value instanceof ObjectNode node) {
                LinkedHashMap map = new LinkedHashMap();
                node.fields().forEachRemaining(stringJsonNodeEntry -> map.put(rewriteKey(stringJsonNodeEntry.getKey()), parseObject(stringJsonNodeEntry.getValue())));
                return map;
            } else {
                return value;
            }
        }

    }

    private static class MapSerialize extends StdSerializer<Map> {

        protected MapSerialize() {
            super(Map.class);
        }


        @Override
        public void serialize(Map map, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
            jgen.writeStartObject();
            map.entrySet().forEach(a -> {
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) a;
                try {
                    jgen.writeObjectField("__" + entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            jgen.writeEndObject();
        }

    }

}
