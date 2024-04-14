package org.wopiserver.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PutFileResponse {
	
	@JsonProperty("LastModifiedTime")
	private String lastModifiedTimestamp;
	
	private PutFileResponse(PutFileResponseBuilder putFileResponseBuilder) {
		this.lastModifiedTimestamp=putFileResponseBuilder.lastModifiedTime;
	}

	public String getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}

	public void setLastModifiedTimestamp(String lastModifiedTime) {
		this.lastModifiedTimestamp = lastModifiedTime;
	}
	
	// Builder
	public static class PutFileResponseBuilder {
		
		private String lastModifiedTime;
		
		public PutFileResponseBuilder(String lastModifiedTime) {
			this.lastModifiedTime=lastModifiedTime;
		}
		
		public PutFileResponse build() {
			return new PutFileResponse(this);
		}
	}
}
