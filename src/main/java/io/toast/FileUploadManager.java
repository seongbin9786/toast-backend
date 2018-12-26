package io.toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
		String path = fileConfig.getServerFilesRootPath() + file.getName();
		
		saveToDrive(file, path);

		Record newRecord = new Record(path);

		return newRecord;
	}
	
	private void saveToDrive(MultipartFile file, String path) {
		try {
			System.out.println("save to drive: " + path);
			file.transferTo(new File(path));
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
		
		System.out.println("fileAsByes: " + fileAsBytes.toString());
		
		return fileAsBytes;
	}
}