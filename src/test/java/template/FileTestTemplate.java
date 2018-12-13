package template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import config.FixturesConfig;

@Ignore
public class FileTestTemplate {

	@Before
	public void createRootDirectory() throws IOException {
		Files.createDirectories(Paths.get(FixturesConfig.FILES_ROOT_PATH));
	}

	@After
	public void cleanUpFiles() throws IOException {
		Files.walk(Paths.get(FixturesConfig.FILES_ROOT_PATH))
			.filter(Files::isRegularFile)
			.map(Path::toFile)
			.forEach(File::delete);
	}

}
