package profiles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(inheritProfiles = false, value = "test")
public class RecordFilePathProfileTest_Test extends RecordFilePathProfileTestTemplate {

    @Value("${rootPath}")
    protected String path;

    @Override
    protected String expectedPath() {
        return "./tests/fixtures/";
    }
}
