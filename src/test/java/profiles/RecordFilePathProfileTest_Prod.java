package profiles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(inheritProfiles = false, value = "prod")
public class RecordFilePathProfileTest_Prod extends RecordFilePathProfileTestTemplate {

    @Value("${rootPath}")
    protected String path;

    @Override
    protected String expectedPath() {
        return "/opt/";
    }
}
