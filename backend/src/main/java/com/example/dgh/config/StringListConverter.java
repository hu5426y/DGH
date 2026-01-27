package com.example.dgh.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("无法序列化图片列表", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            throw new IllegalArgumentException("无法解析图片列表", e);
        }
    }
}
