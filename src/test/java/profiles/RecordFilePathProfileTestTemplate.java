package profiles;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import io.toast.ToastBackendApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ToastBackendApplication.class)
@TestPropertySource("classpath:/application.yml")
@Ignore
public abstract class RecordFilePathProfileTestTemplate {

    @Autowired
    protected Environment env;
    
    @Value("${io.toast.rootPath}")
    protected String path;

    @Test
    public void test() {
        assertEquals(expectedPath(), path);
    }

    protected abstract String expectedPath();
}
