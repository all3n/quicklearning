package com.devhc.quicklearning.history;

import com.devhc.quicklearning.beans.JobMeta;
import java.io.IOException;
import java.util.List;

public abstract class MetaLoader {
  public abstract List<String> listAppIds() throws IOException;
  public abstract JobMeta getJobMeta(String appId) throws IOException;
}
