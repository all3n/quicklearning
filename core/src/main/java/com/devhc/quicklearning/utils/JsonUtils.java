package com.devhc.quicklearning.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonUtils {

  private static final ThreadLocal<Gson> gson = ThreadLocal.withInitial(() ->
      new GsonBuilder().setPrettyPrinting().create());

  public static <T> T parseJson(InputStream is, Class<T> clazz) {
    try {
      return gson.get().fromJson(new InputStreamReader(is), clazz);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static <T> T parseJson(String config, Class<T> clazz) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(config));
      String line = null;
      StringBuilder json = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        json.append(line).append("\n");
      }
      return JSON.parseObject(json.toString(), clazz);
    } catch (IOException e) {
      throw new RuntimeException("reader config error!", e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
        }
      }
    }

  }

  public static <T> String transformObjectToJson(T obj, boolean pretty) {
    return JSON.toJSONString(obj, pretty);
  }
}
