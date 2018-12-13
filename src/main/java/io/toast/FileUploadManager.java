package io.toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadManager {
	
	public static String DOWNLOAD_ROOT_PATH;

	public FileUploadManager() {
	}
	
	public FileUploadManager(String downloadRootPath) {
		setDownloadPath(downloadRootPath);
	}

	@Value("${file.download_root_path}") 
	public void setDownloadPath(String downloadRootPath) {
		DOWNLOAD_ROOT_PATH = downloadRootPath;
	}
	
	public Record saveWithFile(MultipartFile file) {
		String path = DOWNLOAD_ROOT_PATH + "/" + file.getName();
		
		saveToDrive(file, path);

		Record newRecord = new Record(path);

		return newRecord;
	}
	
	private void saveToDrive(MultipartFile file, String path) {
		try {
			file.transferTo(new File(path));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getFileByRecord(Record r) throws IOException {
		
		File fileToDownload = new File(r.getFilePath());
		
		byte[] fileAsBytes = Files.readAllBytes(fileToDownload.toPath());
		
		return fileAsBytes;
	}
}