package io.toast;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Record {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
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

}
