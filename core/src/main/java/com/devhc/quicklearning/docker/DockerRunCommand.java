package com.devhc.quicklearning.docker;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.var;
import org.apache.commons.lang.StringUtils;

/**
 * detail see
 * docker run --help
 */
@Builder
@Data
public class DockerRunCommand {

  private String user;
  private String workerDir;
  private String cudaVisiableDevice;
  private String cpuset;
  private String script;
  private String params;
  private String jobType;
  private String network;
  private int cpuCores;
  private String entrypoint;
  private Map<String, String> volumns;
  private Map<String, String> envs;
  private boolean exposeAll = false;
  private Map<Integer, Integer> expose;
  private long memory;
  // exit auto rm container
  private boolean rmMode;

  private static void buildParam(StringBuilder sb, String param, String value) {
    if (StringUtils.isNotEmpty(value)) {
      sb.append(param).append(value);
    }
    sb.append(" ");
  }

  private static void buildParam(StringBuilder sb, String param, boolean flag) {
    if (flag) {
      sb.append(param);
      sb.append(" ");
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("docker run ");
    buildParam(sb, "--rm", rmMode);
    buildParam(sb, "-P", exposeAll);
    buildParam(sb, "-w=", workerDir);
    buildParam(sb, "-m=", String.valueOf(memory));
    buildParam(sb, "-c=", String.valueOf(cpuCores));
    buildParam(sb, "--entrypoint=", entrypoint);
    buildParam(sb, "--net=", network);
    buildMapParam(sb, "-v", volumns);
    buildMapParam(sb, "-p", envs);
    buildMapParamInt(sb, "-p", expose, ":");

    return sb.toString();
  }

  private static void buildMapParam(StringBuilder sb, String param, Map<String, String> values) {
    if(values == null){
      return;
    }
    for (var es : values.entrySet()) {
      // --name k=v
      sb.append(param).append(" ").append(es.getKey()).append("=").append(es.getValue()).append(" ");
    }
  }
   private static void buildMapParamInt(StringBuilder sb, String param, Map<Integer, Integer> values, String split) {
    if(values == null){
      return;
    }
    for (var es : values.entrySet()) {
      // --name k:v
      sb.append(param).append(" ").append(es.getKey()).append(split).append(es.getValue()).append(" ");
    }
  }
}
