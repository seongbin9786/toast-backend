package io.toast;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(of = "id")
public class Record {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	private String filePath;
	
	public Record() {
	}
	
	public Record(Long id) {
		this.id = id;
	}

	public Record(String filePath) {
		this.filePath = filePath;
	}

	public Long getId() {
		return id;
	}

	public String getFilePath() {
		return filePath;
	}

}
