package com.yoyo.derclown.apikitdemo.APIKit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiHelpers {
    public static Object transformerResponseDataToTargetMoelClass(Object responseData, Class targetModelClass) {
        if (responseData == null) return null;

        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        String jsonString = gson.toJson(responseData);

        // 如果是数组
        if (responseData instanceof ArrayList) {
            List responseList = (ArrayList)responseData;    // 强转
            List result = new ArrayList<>();

            JsonArray jsonArray = parser.parse(jsonString).getAsJsonArray();
            for(JsonElement obj : jsonArray ){
                Object targetModel = gson.fromJson( obj , targetModelClass);
                result.add(targetModel);
            }

            return result;
        } else {
            Object targetModel = gson.fromJson(jsonString, targetModelClass);
            return targetModel;
        }
    }

    public static Map signParamsWithOtherParams(Map params) {
        return null;
    }
}
