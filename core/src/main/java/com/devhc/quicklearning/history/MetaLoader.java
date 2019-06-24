package com.devhc.quicklearning.history;

import com.devhc.quicklearning.beans.JobMeta;
import com.devhc.quicklearning.beans.Pagination;
import com.devhc.quicklearning.beans.PaginationResult;
import com.devhc.quicklearning.history.beans.HistoryJob;
import java.io.IOException;
import java.util.List;

public abstract class MetaLoader {
  public abstract PaginationResult<HistoryJob> listApps(Pagination pageRequest) throws IOException;
  public abstract JobMeta getJobMeta(String appId) throws IOException;
}
