package template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import io.toast.config.FileConfig;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:/application.yml")
@ActiveProfiles("test")
@EnableConfigurationProperties(FileConfig.class)
@Ignore
public class FileTestTemplate {

	@Autowired
	protected FileConfig fileConfig;
	
	@Before
	public void createRootDirectory() throws IOException {
		
		System.out.println(fileConfig);
		
		Files.createDirectories(Paths.get(fileConfig.getFilesRootPath()));
	}

	@After
	public void cleanUpFiles() throws IOException {
		Files.walk(Paths.get(fileConfig.getFilesRootPath()))
			.filter(Files::isRegularFile)
			.map(Path::toFile)
			.forEach(File::delete);
	}

}
