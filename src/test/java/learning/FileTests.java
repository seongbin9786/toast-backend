package learning;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import config.FixturesConfig;
import template.FileTestTemplate;

public class FileTests extends FileTestTemplate {

	@Test
	public void 파일을_작성할_수_있다() throws Exception {

		// given
		final String filePath = FixturesConfig.FILES_ROOT_PATH + "test.txt";
		final File fileToCreate = new File(filePath);

		// when
		fileToCreate.createNewFile();

		// then
		Path p = Paths.get(filePath);
		assertTrue(Files.exists(p));
	}

}
