package org.wopiserver.configuration.pojo;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
	private String baseDir;
	private String codeURL;
	private Map<String,String> endPoints;
	private String proxyHost;
	
	public Configuration() {
		endPoints=new HashMap<String,String>();
	}
	
	public String getBaseDir() {
		return baseDir;
	}
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	public String getCodeURL() {
		return codeURL;
	}
	public void setCodeURL(String codeURL) {
		this.codeURL = codeURL;
	}
	public Map<String, String> getEndPoints() {
		return endPoints;
	}
	public void setEndPoints(Map<String, String> endPoints) {
		this.endPoints = endPoints;
	}
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
}
