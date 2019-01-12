package io.toast;

import io.toast.config.FileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

        return new Record(path, file.getOriginalFilename());
    }

    private String createPath(String serverFilesRootPath) {
        return serverFilesRootPath + getFolderNameForToday() + UUID.randomUUID();
    }

    private String getFolderNameForToday() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");

        return today.format(formatter);
    }

    private void saveToDrive(MultipartFile file, String path) {
        try {
            File filePath = new File(path).getCanonicalFile(); // canonical 이 중요함
            createParentDirectory(filePath);
            System.out.println("save to drive: " + path);
            file.transferTo(filePath);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void createParentDirectory(File filePath) throws IOException {
        File parentFile = filePath.getParentFile();
        Path parentPath = parentFile.toPath();
        System.out.println("parentFile: " + parentFile.toString());
        System.out.println("parentPath: " + parentPath.toString());

        boolean pathCreated = parentFile.mkdirs();
        System.out.println("pathCreated: " + pathCreated);
        boolean parentExists = Files.exists(parentPath);
        System.out.println("parentExists:" + parentExists + " | parentFile: " + parentFile.toString());
        if (!parentExists) {
            throw new IOException("폴더 생성에 실패했습니다.");
        }
    }

    public byte[] getFileByRecord(Record r) throws IOException {
        System.out.println("getFilePathFromRecord: " + r.getFilePath());

        File fileToDownload = new File(r.getFilePath());

        return Files.readAllBytes(fileToDownload.toPath());
    }
}