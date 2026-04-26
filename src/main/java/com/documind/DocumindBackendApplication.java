package com.documind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

import com.documind.config.FileStorageProperties;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(FileStorageProperties.class)
public class DocumindBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumindBackendApplication.class, args);
	}

}
