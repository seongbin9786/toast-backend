package io.toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.toast.config.FileConfig;

@Component
public class FileUploadManager {
	
	private FileConfig fileConfig;

	@Autowired
	public FileUploadManager(FileConfig fileConfig) {
		this.fileConfig = fileConfig;
	}

	public Record saveWithFile(MultipartFile file) {
		String path = createPath(fileConfig.getServerFilesRootPath());
		
		saveToDrive(file, path);

		return new Record(path, file.getName());
	}

	private String createPath(String serverFilesRootPath) {
		return serverFilesRootPath + getFolderNameForToday() + UUID.randomUUID();
	}

	private String getFolderNameForToday() {
		LocalDate today = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
		String path = today.format(formatter);

		return path;
	}

	private void saveToDrive(MultipartFile file, String path) {
		try {
			System.out.println("save to drive: " + path);
			File savedPath = new File(path);
			savedPath.getParentFile().mkdirs();
			file.transferTo(savedPath);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getFileByRecord(Record r) throws IOException {
		
		System.out.println("getFilePathFromRecord: " + r.getFilePath());
		
		File fileToDownload = new File(r.getFilePath());
		
		byte[] fileAsBytes = Files.readAllBytes(fileToDownload.toPath());
		
		System.out.println("fileAsByes: " + fileAsBytes);
		
		return fileAsBytes;
	}
}