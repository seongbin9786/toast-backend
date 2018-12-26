package io.toast.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Setter
@ToString
public class FileConfig {

	private static final String SERVER_PATH = "server/";

	private static final String CLIENT_PATH = "client/";
	
	@Getter
	private String rootPath;

	public String getFilesRootPath() {
		return getRootPath() + "files/";
	}
	
	public String getServerFilesRootPath() {
		return getFilesRootPath() + SERVER_PATH;
	}
	
	public String getClientFilesRootPath() {
		return getFilesRootPath() + CLIENT_PATH;
	}
}
