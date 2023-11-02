package com.budget.project.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class Utils {
    public static Map<String, Object> toMap(Object object) {
        return new ObjectMapper().convertValue(object, new TypeReference<>() {});
    }
}
