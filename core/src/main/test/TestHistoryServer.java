import com.devhc.quicklearning.history.HistoryServer;
import com.devhc.quicklearning.master.AppMaster;
import java.io.File;
import java.io.IOException;

public class TestHistoryServer {

  public static void main(String[] args) throws IOException {
    String appDir = new File(".").getCanonicalPath();
    String webDir = appDir + "/" + "public/target/historyserver";
    HistoryServer.main(new String[]{
        "-w", webDir, "-p", "44444"
    });
  }
}
