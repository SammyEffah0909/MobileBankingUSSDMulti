package com.etz.gh.ussd.mobile.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class PropsCache {
  private final Properties prop = new Properties();
  
  public static final boolean IS_DEBUG_MODE = false;
  
  public static final String LOG4J_DEBUG = "cfg\\log4j.properties";
  
  public static final String PROPS_DEBUG = "cfg/mobileBanking.properties";
  
  public static final String LOG4J_PROD = "cfg\\log4j.properties";
  
  public static final String PROPS_PROD = "cfg/mobileBanking.properties";
  
  String log4jPath = "cfg\\log4j.properties";
  
  String configPath = "cfg/mobileBanking.properties";
  
  private PropsCache() {
    try {
      this.prop.load(new FileInputStream(new File(this.configPath)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  private static class LazyHolder {
    private static final PropsCache INSTANCE = new PropsCache();
  }
  
  public static PropsCache getInstance() {
    return LazyHolder.INSTANCE;
  }
  
  public String getProperty(String key) {
    return this.prop.getProperty(key);
  }
  
  public Set<String> getAllPropertyNames() {
    return this.prop.stringPropertyNames();
  }
  
  public boolean containsKey(String key) {
    return this.prop.containsKey(key);
  }
}
