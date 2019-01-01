package learning

import io.toast.ToastBackendApplication
import org.springframework.boot.test.context.SpringBootTest
import template.FileTestTemplate

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@SpringBootTest(classes = ToastBackendApplication)
class FileTests extends FileTestTemplate {

    @Override
    def 필요하면_FileConfig_를_덮어쓰기() {}

    def "파일을 작성할 수 있다"() {

        given:
        String filePath = fileConfig.getRootPath() + "test.txt"
        File fileToCreate = new File(filePath)

        when:
        fileToCreate.createNewFile()

        then:
        Path p = Paths.get(filePath)
        assert Files.exists(p)
    }

}
