package com.ljf.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    private JSONUtil(){}

    /**
     * 将JSON格式字符串转换为Java对象
     */
    public static <T> T jsonToObj(Class<T> objClass,String jsonStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // T t = mapper.readValue(jsonStr, objClass);
        return mapper.readValue(jsonStr, objClass);
    }

    /**
     * 将Java对象装换为JSON格式字符串
     * */
    public static String objToJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }
}
