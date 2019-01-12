package io.toast;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Transactional
@RestController
@RequestMapping("/records")
public class RecordController {

	private static final String AUDIO_PREFIX = "audio/";

	@Autowired
	private RecordRepository repo;

	private FileUploadManager manager;

	@Autowired
	public void setFileUploadManager(FileUploadManager manager) {
		this.manager = manager;
	}

	@ApiOperation(value = "녹음 파일 개별 조회", notes = "ID에 대응하는 녹음 파일을 제공")
	@ApiResponses({
			@ApiResponse(code = 200, message = "녹음 파일을 정상적으로 제공"),
			@ApiResponse(code = 404, message = "녹음 파일이 없음")
	})
	@GetMapping("/{id}")
	public ResponseEntity<byte[]> download(@ApiParam(name = "id", value = "녹음 파일의 ID") @PathVariable Long id) throws Exception {
		Optional<Record> rOptional = repo.findById(id);
		Record r = rOptional.orElseThrow(NoRecordException::new);

		byte[] content;
		try {
			content = manager.getFileByRecord(r);
		} catch (NoSuchFileException e) {
			throw new CannotFindRecordFileException();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment;filename=" + r.getOriginalFileName());

		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "녹음 파일 전체 조회", notes = "녹음 파일의 전체 목록을 제공")
	@ApiResponses({
			@ApiResponse(code = 200, message = "ID에 대응하는 녹음 파일을 제공")
	})
	@GetMapping
	public List<Record> getAll() {
		return repo.findAll();
	}

	@ApiOperation(value = "녹음 파일 업로드", notes = "녹음 파일을 업로드")
	@ApiResponses({
			@ApiResponse(code = 200, message = "녹음 파일에 성공적으로 업로드됨"),
			@ApiResponse(code = 400, message = "지원하지 않는 포멧으로 업로드함. 저장하지 않음")
	})
	@PostMapping
	public Record upload(@ApiParam(name = "file", value = "녹음 파일") @RequestPart("file") MultipartFile file) {
		assertFileIsAudio(file);

		Record newRecord = manager.saveWithFile(file);

		return repo.save(newRecord);
	}

	@ApiOperation(value = "녹음 파일 삭제", notes = "녹음 파일을 제거")
	@ApiResponses({
			@ApiResponse(code = 200, message = "녹음 파일을 성공적으로 제거함"),
			@ApiResponse(code = 400, message = "존재하지 않는 녹음 파일 ID 이므로 제거하지 못함")
	})
	@DeleteMapping("/{id}")
	public void delete(@ApiParam(name = "id", value = "녹음 파일의 ID") @PathVariable Long id) {
		Optional<Record> toDelete = repo.findById(id);
		Record r = toDelete.orElseThrow(NoRecordException::new);

		repo.delete(r);
	}

	private void assertFileIsAudio(MultipartFile file) {
		// original filename 은 key 에 상관 없음
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null)
			throw new BadFileUploadedException();
		System.out.println("original Filename: " + originalFilename);

		try {
			String probedContentType = Files.probeContentType(Paths.get(originalFilename));
			System.out.println("contentType: " + probedContentType);
			if (!probedContentType.startsWith(AUDIO_PREFIX)) {
				throw new BadFileUploadedException();
			}
		} catch (IOException e) {
			throw new BadFileUploadedException();
		}
	}
}
