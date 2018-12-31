package template

import io.toast.config.FileConfig
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

@TestPropertySource("classpath:/application.yml")
@ActiveProfiles("test")
@EnableConfigurationProperties(FileConfig.class)
@Ignore
class FileTestTemplate extends Specification {

	@Autowired
	protected FileConfig fileConfig
	
	def createRootDirectory() {
		
		System.out.println(fileConfig)
		
		Files.createDirectories(Paths.get(fileConfig.getFilesRootPath()))
	}

	def cleanUpFiles() throws IOException {
        def rootPath = Paths.get(fileConfig.getFilesRootPath())

		Files.walk(rootPath)
			.filter({ currentPath -> Files.isRegularFile(currentPath)})
			.map({ currentPath -> currentPath.toFile() })
			.forEach({ currentFile -> currentFile.delete() })

		/*
			.filter({ Files files -> files.isRegularFile })
			.map({ Path path -> path.toFile })
			.forEach({ File file -> file.delete })
		 */
	}

}
