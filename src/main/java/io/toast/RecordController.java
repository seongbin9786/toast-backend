package io.toast;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Transactional
@RestController
@RequestMapping("/records")
public class RecordController {

	public static String DOWNLOAD_ROOT_PATH;

	@Autowired
	private RecordRepository repo;

	@Value("${file.download_root_path}") 
	public void setDownloadPath(String path) {
		DOWNLOAD_ROOT_PATH = path;
	}
	
	@PostMapping
	public Record upload(MultipartHttpServletRequest request) throws IOException {
		
		MultipartFile file = getFileFromRequest(request);
		
		String path = DOWNLOAD_ROOT_PATH + "/" + file.getName();
		
		saveToDrive(file, path);

		return repo.save(new Record(path));
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

	private MultipartFile getFileFromRequest(MultipartHttpServletRequest request) {
		
		String key;
		
		try {
			key = request.getMultiFileMap().keySet().iterator().next();
		} catch (NoSuchElementException e) {			
			throw new NoFileUploadedException();
		}
		
		MultipartFile file = request.getFile(key);
		
		return file;
	}

}
