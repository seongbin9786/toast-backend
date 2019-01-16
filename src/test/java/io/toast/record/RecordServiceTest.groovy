package io.toast.record

import io.toast.config.FileConfig
import io.toast.records.application.FileUploadManager
import io.toast.records.domain.Record
import org.springframework.web.multipart.MultipartFile
import spock.lang.Shared
import template.FileTestTemplate

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecordServiceTest extends FileTestTemplate {

    static final 파일명 = "filename.mp3"

    @Shared
    FileUploadManager fileUploadManager

    @Shared
    MultipartFile 파일

    def setup() {
        필요하면_FileConfig_를_덮어쓰기()

        fileUploadManager = new FileUploadManager(fileConfig)

        파일 = 테스트용_파일_생성하기(클라이언트_파일_디렉토리() + "/" + 파일명)
    }

    @Override
    def 필요하면_FileConfig_를_덮어쓰기() {
        fileConfig = new FileConfig()
        fileConfig.rootPath = "./tests/fixtures/"
    }

    def "업로드된 파일을 다운로드 시 byte[]를 제공해야 한다"() {
        given:
        Record saved = fileUploadManager.saveWithFile(파일)

        when:
        byte[] arrayFromManager = fileUploadManager.getFileByRecord(saved)

        then:
        assert arrayFromManager != null
    }

    def "업로드된 파일을 저장 시 오늘의 주소에 해당하는 폴더에 저장되어야 한다"() {
        when: "파일을 저장한다"
        Record saved = fileUploadManager.saveWithFile(파일)

        then: "파일 경로가 오늘 주소를 담는다"
        assert saved.getFilePath().contains(getFolderForToday())
    }

    def "업로드된 파일을 저장 시 겹치지 않는 랜덤 파일 명을 사용한다"() {
        when: "파일을 저장한다"
        Record saved = fileUploadManager.saveWithFile(파일)

        then: "파일이 랜덤 파일 명을 사용한다"
        assert !saved.getFilePath().contains(파일명)
    }

    def "랜덤 파일 명은 최소 20자여야 한다"() {
        when: "파일을 저장한다"
        Record saved = fileUploadManager.saveWithFile(파일)

        then: "파일이 랜덤 파일 명을 사용한다"
        def filenameIndex = saved.getFilePath().lastIndexOf("/") + 1
        def filename = saved.getFilePath().substring(filenameIndex)
        assert filename.length() >= 20
    }

    def "업로드된 파일을 저장 시 원본 파일 명을 보존한다"() {
        when: "파일을 저장한다"
        Record saved = fileUploadManager.saveWithFile(파일)

        then: "저장 후 반환한 Record 가 원본 파일 명을 갖는다"
        assert saved.originalFileName == 파일명
    }

    def getFolderForToday() {
        def today = LocalDate.now()
        def formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        today.format(formatter)
    }
}
