package org.wopiserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.wopiserver.documents.impl.AutoExposeDocument;
import org.wopiserver.documents.itf.DocumentService;

@Configuration
@EnableScheduling
public class WopiServerApplicationConfiguration {

	@Bean
	public DocumentService documentService() {
		return new AutoExposeDocument();
	}	
}