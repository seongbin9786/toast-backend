package io.toast;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@ApiModel(description = "개별 녹음 파일의 메타데이터")
@Entity
@Getter
@EqualsAndHashCode(of = "id")
public class Record {

	@ApiModelProperty(value = "녹음 파일의 ID", example = "1", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// TODO: filePath가 JsonIgnore가 되는 이유가 되는 테스트가 필요
	@JsonIgnore
	private String filePath;

	@ApiModelProperty(value = "파일명", example = "해커스 토스 Chapter 1.mp3", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
	private String originalFileName;

	// TODO: fileSize가 Json에 필요

	public Record() {
	}
	
	public Record(Long id) {
		this.id = id;
	}

	public Record(String filePath, String originalFileName) {
		this.filePath = filePath;
		this.originalFileName = originalFileName;
	}
}
