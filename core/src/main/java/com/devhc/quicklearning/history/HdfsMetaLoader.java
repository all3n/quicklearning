package com.devhc.quicklearning.history;

import com.devhc.quicklearning.beans.JobMeta;
import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.utils.JsonUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.var;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

@Singleton
public class HdfsMetaLoader extends MetaLoader {

  private final QuickLearningConf conf;
  private final FileSystem fs;
  private final String metaBase;

  public HdfsMetaLoader() throws IOException {
    this.conf = new QuickLearningConf();
    this.metaBase = conf.get(QuickLearningConf.META_CONFIG);
    this.fs = FileSystem.get(conf);
  }

  @Override
  public List<String> listAppIds() throws IOException {
    return Arrays.stream(fs.listStatus(new Path(metaBase)))
        .map(t -> t.getPath().getName()).collect(
            Collectors.toList());
  }

  @Override
  public JobMeta getJobMeta(String appId) throws IOException {
    String metaJson = metaBase + "/" + appId + "/meta.json";
    JobMeta meta = null;
    try (var is = fs.open(new Path(metaJson))) {
      meta = JsonUtils.parseJson(is, JobMeta.class);
    }
    return meta;
  }
}
