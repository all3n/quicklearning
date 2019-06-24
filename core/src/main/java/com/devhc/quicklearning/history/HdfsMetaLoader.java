package com.devhc.quicklearning.history;

import com.devhc.quicklearning.beans.JobMeta;
import com.devhc.quicklearning.beans.Pagination;
import com.devhc.quicklearning.beans.PaginationResult;
import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.history.beans.HistoryJob;
import com.devhc.quicklearning.utils.JsonUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
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
  public PaginationResult<HistoryJob> listApps(Pagination pageRequest) throws IOException {
    var listSt = fs.listStatus(new Path(metaBase));
    var listApps = Arrays.stream(listSt)
        .map(t -> HistoryJob.builder().app_id(t.getPath().getName())
            .timestamp(t.getModificationTime()).build())
        .sorted((o1, o2) -> o1.getTimestamp() > o2.getTimestamp() ? 1 : 0)
        .collect(Collectors.toList());

    var totalPages = (int) Math.ceil(listSt.length / pageRequest.getPageSize());
    var curPage = Math.min(Math.max(pageRequest.getCurrent(), 0),totalPages);
    pageRequest.setCurrent(curPage);
    var pageSize = Math.max(pageRequest.getPageSize(), 1);
    pageRequest.setPageSize(pageSize);

    var startIdx = pageRequest.getCurrent() * pageRequest.getPageSize();

    var subListApps = listApps.subList(startIdx,
        Math.min(startIdx + pageRequest.getPageSize()
        , listSt.length));

    pageRequest.setTotal(listSt.length);
    pageRequest.setTotalPages(totalPages);

    return new PaginationResult<>(subListApps, pageRequest);
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
