package org.wopiserver.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PutRelativeFileResponse {
	
	@JsonProperty("Name")
	private String name;
	
	@JsonProperty("Url")
	private String url;
	
	private PutRelativeFileResponse(PutRelativeFileResponseBuilder putRelativeFileResponseBuilder) {
		this.name=putRelativeFileResponseBuilder.name;
		this.url=putRelativeFileResponseBuilder.url;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	// Builder
	public static class PutRelativeFileResponseBuilder {
		
		private String name;
		private String url;
		
		public PutRelativeFileResponseBuilder(String name, String url) {
			this.name=name;
			this.url=url;
		}
		
		public PutRelativeFileResponse build() {
			return new PutRelativeFileResponse(this);
		}
	}
}
