package template

import io.toast.config.FileConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

@TestPropertySource("classpath:/application.yml")
@ActiveProfiles("test")
@EnableConfigurationProperties(FileConfig.class)
@Ignore
abstract class FileTestTemplate extends Specification {

	@Autowired
	FileConfig fileConfig

	def 서버_파일_디렉토리() {
		return fileConfig.getServerFilesRootPath()
	}

	def 클라이언트_파일_디렉토리() {
		return fileConfig.getClientFilesRootPath()
	}

	abstract def 필요하면_FileConfig_를_덮어쓰기()

	def setup() {
		필요하면_FileConfig_를_덮어쓰기()
		디렉토리_생성하기()
	}

	def 디렉토리_생성하기() {
		System.out.println(서버_파일_디렉토리())
		System.out.println(클라이언트_파일_디렉토리())
		Files.createDirectories(Paths.get(서버_파일_디렉토리()))
		Files.createDirectories(Paths.get(클라이언트_파일_디렉토리()))
	}

	static def 파일생성하기(String 경로) throws Exception {
		final MB = 3
		RandomAccessFile randomAccessFile = new RandomAccessFile(경로, "rw")
		randomAccessFile.setLength(1024 * 1024 * MB)
		randomAccessFile.close()

		new File(경로)
	}

	static def 테스트용_파일_생성하기(String 경로) throws Exception {
		File toConvert = 파일생성하기(경로)
		FileInputStream fis = new FileInputStream(toConvert)

		new MockMultipartFile(toConvert.getName(), fis)
	}

	static def getContentType(String ext) throws IOException {
		Files.probeContentType(Paths.get("a" + ext))
	}

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
