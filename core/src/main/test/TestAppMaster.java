import com.devhc.quicklearning.master.AppMaster;
import java.io.File;
import java.io.IOException;

public class TestAppMaster {

  public static void main(String[] args) throws IOException {
    String appDir = new File(".").getCanonicalPath();
    String webDir = appDir + "/" + "public/dist";
    AppMaster.main(new String[]{
        "-w", webDir, "-p", "33333", "-c", appDir + "/test/test.json", "--stopAtFinished", "0"
    });
  }
}
