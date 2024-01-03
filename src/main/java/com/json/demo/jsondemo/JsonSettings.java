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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class JsonSettings {

    private ObjectMapper objectMapper;

    @PostConstruct
    void postConstruct() {
        objectMapper = new ObjectMapper();
        SimpleModule mapModule = new SimpleModule();
        MapDeserializer mapDeserializer = new MapDeserializer(Map.class);
//        mapModule.addDeserializer(Map.class, mapDeserializer);
        mapModule.addDeserializer(Item.class, new ItemDeserializer(Item.class));
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
            return key + "===";
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


    private static class ItemDeserializer extends StdDeserializer<Item> {
        protected ItemDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Item deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            Item item = new Item();
            node.fields().forEachRemaining(stringJsonNodeEntry -> writeField2Object(rewriteKey(stringJsonNodeEntry.getKey()), parseObject(stringJsonNodeEntry.getValue()), item));
            return item;
        }

        private JsonNode parseObject(JsonNode value) {
            return value;
        }

        private static String rewriteKey(String key) {
            return "__" + key;
        }


        private static void copyPropertiesFromMap(Map source, Object target) throws BeansException {

            Assert.notNull(source, "Source must not be null");
            Assert.notNull(target, "Target must not be null");

            Class<?> actualEditable = target.getClass();

            PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);

            for (PropertyDescriptor targetPd : targetPds) {
                Method writeMethod = targetPd.getWriteMethod();
                if (writeMethod != null) {
                    try {

                        Object value = source.get(rewriteKey(targetPd.getName()));
                        ReflectionUtils.makeAccessible(writeMethod);
                        writeMethod.invoke(target, value);
                    } catch (Throwable ex) {
                        throw new FatalBeanException("Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                    }
                }
            }
        }

        static ConcurrentHashMap<Class<?>, Map<String, PropertyDescriptor>> cachedFields = new ConcurrentHashMap<Class<?>, Map<String, PropertyDescriptor>>();

        private static void writeField2Object(String fieldName, Object value, Object target) throws BeansException {

            Assert.notNull(fieldName, "fieldName must not be null");
            Assert.notNull(target, "Target must not be null");

            Class<?> actualEditable = target.getClass();

            Method writeMethod = Optional.of(cachedFields.computeIfAbsent(actualEditable, key -> {
                PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);
                return Arrays.stream(targetPds).map(propertyDescriptor -> new AbstractMap.SimpleEntry<>(propertyDescriptor.getName(), propertyDescriptor)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
            })).map(map -> map.get(fieldName)).map(PropertyDescriptor::getWriteMethod).orElseThrow(() -> new BeansException(String.format("Not found field %s in class %s", fieldName, target.getClass())) {
            });


            try {
                ReflectionUtils.makeAccessible(writeMethod);
                writeMethod.invoke(target, value);
            } catch (Throwable ex) {
                throw new FatalBeanException("Could not copy property '" + fieldName + "' from source to target", ex);
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


    public static class User {
        public int __id;
        public String __name;

        public int get__id() {
            return __id;
        }

        public void set__id(int __id) {
            this.__id = __id;
        }

        public String get__name() {
            return __name;
        }

        public void set__name(String __name) {
            this.__name = __name;
        }
    }

    public static class Item {
        public int __id;
        public String __itemName;
        public User __owner;

        public int get__id() {
            return __id;
        }

        public void set__id(int __id) {
            this.__id = __id;
        }

        public String get__itemName() {
            return __itemName;
        }

        public void set__itemName(String __itemName) {
            this.__itemName = __itemName;
        }

        public User get__owner() {
            return __owner;
        }

        public void set__owner(User __owner) {
            this.__owner = __owner;
        }
    }
}
