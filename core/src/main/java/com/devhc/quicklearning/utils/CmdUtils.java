package com.devhc.quicklearning.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdUtils {

  private static Logger LOG = LoggerFactory.getLogger(CmdUtils.class);


  public static void printStreamLog(final Process processor) {
    printStreamLog(processor, true);
  }

  public static void printStreamLog(final Process processor, boolean print_flag) {
    Thread std = new Thread(new Runnable() {
      @Override
      public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(processor.getInputStream()));
        String line = null;
        try {
          while ((line = in.readLine()) != null && print_flag) {
            System.out.println(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            in.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
    std.setDaemon(true);
    Thread err = new Thread(new Runnable() {
      @Override
      public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(processor.getErrorStream()));
        String line = null;
        try {
          while ((line = in.readLine()) != null && print_flag) {
            System.err.println(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            in.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
    err.setDaemon(true);
    std.start();
    err.start();
  }

  public static int exeCmd3(String cmd, int retryTimes){
    CommandLine line = CommandLine.parse(cmd);
    DefaultExecutor exec = new DefaultExecutor();
    int exitCode = 0;
    while(retryTimes > 0) {
      try {
        exec.setExitValue(1);
        exitCode = exec.execute(line);
        if(exitCode == 0) {
          break;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      retryTimes--;
    }
    return exitCode;
  }
  public static int exeCmd(String cmd, int retryTimes) {
    long startTime = System.currentTimeMillis();
    int exitCode = 0;
    while (retryTimes > 0) {
      retryTimes--;
      try {
        Process p = Runtime.getRuntime().exec(cmd);
        printStreamLog(p);
        exitCode = p.waitFor();
        LOG.info("cmd [{}] cost {} ms status [{}] ", cmd, System.currentTimeMillis() - startTime,
            exitCode);
        if (exitCode == 0) {
          break;
        }
      } catch (Exception e) {
        LOG.error(cmd + " error!", e);
      }
    }
    return exitCode;
  }
}
