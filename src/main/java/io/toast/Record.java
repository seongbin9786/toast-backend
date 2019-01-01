package io.toast;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
public class Record {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	private String filePath;

	private String originalFileName;
	
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
