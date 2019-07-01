package com.devhc.quicklearning.utils;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Created by wanghch on 2019/6/27.
 */
public class ArgsUtils {
  public static <T> T parseArgument(T obj, String args[]) {
    CmdLineParser parser = new CmdLineParser(obj);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      e.printStackTrace();
      parser.printUsage(System.err);
      System.exit(-1);
    }
    return obj;
  }
}
