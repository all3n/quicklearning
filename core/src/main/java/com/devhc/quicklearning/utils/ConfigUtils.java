package com.devhc.quicklearning.utils;

import com.alibaba.fastjson.JSON;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigUtils {

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

}
