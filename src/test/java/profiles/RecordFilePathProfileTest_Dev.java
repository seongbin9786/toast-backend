package profiles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(inheritProfiles = false, value = "dev")
public class RecordFilePathProfileTest_Dev extends RecordFilePathProfileTestTemplate {

    @Value("${rootPath}")
    protected String path;

    @Override
    protected String expectedPath() {
        return "./temp/";
    }
}
