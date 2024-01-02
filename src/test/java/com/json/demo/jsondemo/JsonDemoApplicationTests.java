package com.json.demo.jsondemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;


@SpringBootTest
class JsonDemoApplicationTests
{
	@Autowired
	JsonSettings jsonSettings;

	@Test
	void contextLoads() throws JsonProcessingException {
		String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\", \"aa\" : 123 , \"inner\": { \"color\" : \"Black\", \"type\" : \"BMW\", \"aa\" : 123 }}";
		Map car = jsonSettings.getObjectMapper().readValue(json, Map.class);

		System.out.println(car);

		String str = jsonSettings.getObjectMapper().writeValueAsString(car);
		System.out.println(str);
	}

	@Test
	void test2() throws JsonProcessingException {
//		String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
//		ObjectMapper objectMapper = new ObjectMapper();
//		JsonNode rootNode = objectMapper.readTree(json);
//
//		int count = 1;
//		for (JsonNode node : rootNode) {
//			if (node instanceof TextNode textNode){
//				textNode.
//			}
//			ObjectNode objectNode = (ObjectNode) node;
//			objectNode.fields().forEachRemaining(entry -> {
//				String key = entry.getKey();
//				JsonNode value = entry.getValue();
//				objectNode.put("_" + key, value.asText());
//			});
//		}
//		String output = objectMapper.writeValueAsString(rootNode);
	}

	public static class User {
		public int id;
		public String name;


	}
	public static class Item {
		public int id;
		public String itemName;
		public User owner;
	}

}
