package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppContainerModule;
import com.devhc.quicklearning.utils.CmdUtils;
import com.devhc.quicklearning.utils.Constants;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XdlContainerRunner {

  private static Logger LOG = LoggerFactory.getLogger(XdlContainerRunner.class);
  @Inject
  XdlArgs args;

  public static void main(String[] args) {
    try {
      List<Module> moduleList = Lists.newArrayList();
      moduleList.add(new AppContainerModule<XdlArgs>(args, XdlArgs.class));
      XdlContainerRunner runner = Guice.createInjector(moduleList)
          .getInstance(XdlContainerRunner.class);
      System.exit(runner.start());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private int start() {
    LOG.info("args:{}", args);
    String entry = args.getEntry();

    String jobType = args.getJobType();

    int exitCode = 0;
    if (jobType.equals("worker")) {
      if (StringUtils.isEmpty(entry)) {
        throw new RuntimeException("worker must has entry script");
      }

      entry = entry.replace("{PYTHON}", Constants.ENV_DIR + "/bin/python")
          .replace("{APP_DIR}", Constants.APP_DIR);
      String argsPart = args.getArgs() == null ? "": args.getArgs();
      String command = String.format("%s %s", entry, argsPart);
      LOG.info("command:{}", command);
      exitCode = CmdUtils.exeCmd(command, 1);
    }

    return exitCode;
  }
}
