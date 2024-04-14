package org.wopiserver.configuration.impl;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wopiserver.configuration.itf.ConfigurationService;
import org.wopiserver.configuration.pojo.Configuration;

import ch.qos.logback.classic.Logger;

@Service
public class InMemoryConfigurationImpl implements ConfigurationService {
	
	private Configuration conf;
	
	private Logger logger = (Logger)LoggerFactory.getLogger(this.getClass());
	
	public InMemoryConfigurationImpl() {
		conf=new Configuration();
	}

	@Override
	public String getBaseDirectory() {
		return conf.getBaseDir();
	}

	@Override
	public String toString() {
		String str="Configuration => ( Base Directory: "+getBaseDirectory()+" )";
		return str;
	}

	@Override
	public void setConfiguration(String baseDirectory) {
		conf.setBaseDir(baseDirectory);		
		logger.info(toString());
	}
}
