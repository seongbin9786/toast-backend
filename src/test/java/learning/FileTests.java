package learning;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileTests {

	private static final String ROOT_PATH = "./tests/fixtures/files/";

	@Before
	public void createRootDirectory() throws IOException {
		Files.createDirectories(Paths.get(ROOT_PATH));
	}

	@After
	public void cleanUpFiles() throws IOException {
		Files.walk(Paths.get(ROOT_PATH))
			.filter(Files::isRegularFile)
			.map(Path::toFile)
			.forEach(File::delete);
	}

	@Test
	public void 파일을_작성할_수_있다() throws Exception {

		// given
		final String filePath = ROOT_PATH + "test.txt";
		final File fileToCreate = new File(filePath);

		// when
		fileToCreate.createNewFile();

		// then
		Path p = Paths.get(filePath);
		assertTrue(Files.exists(p));
	}

}
