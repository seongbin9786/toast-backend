package io.toast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Transactional
@RestController
@RequestMapping("/records")
public class RecordController {

	public static final String AUDIO_PREFIX = "audio/";

	@Autowired
	private RecordRepository repo;
	
	private FileUploadManager manager;
	
	@Autowired
	public void setFileUploadManager(FileUploadManager manager) {
		this.manager = manager;
	}
	
	@GetMapping("/{id}")
	public byte[] download(@PathVariable Long id) throws Exception {
		Optional<Record> rOptional = repo.findById(id);
		Record r = rOptional.orElseThrow(() -> new NoRecordException());

		return manager.getFileByRecord(r);
	}
	
	@GetMapping
	public List<Record> getAll() {
		return repo.findAll();
	}
	
	@PostMapping
	public Record upload(MultipartHttpServletRequest request) throws IOException {
		
		MultipartFile file = getFileFromRequest(request);
		
		assertFileIsAudio(file);
		
		Record newRecord = manager.saveWithFile(file);
		
		return repo.save(newRecord);
	}

	private void assertFileIsAudio(MultipartFile file) throws IOException {
		String filename = file.getName();
		String contentType = Files.probeContentType(Paths.get(filename));
		if (!contentType.startsWith(AUDIO_PREFIX))
			throw new BadFileUploadedException();
	}

	private MultipartFile getFileFromRequest(MultipartHttpServletRequest request) {
		
		String key;
		
		try {
			key = request.getMultiFileMap().keySet().iterator().next();
		} catch (NoSuchElementException e) {			
			throw new NoFileUploadedException();
		}
		
		return request.getFile(key);
	}

}
