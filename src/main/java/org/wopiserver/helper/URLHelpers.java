package org.wopiserver.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpServletRequest;

public class URLHelpers {
	
	private static Logger logger = (Logger)LoggerFactory.getLogger(URLHelpers.class.getName());

	public static String createURL(String documentId, String accessToken, String localHost) {
		return "http://"+localHost+"/wopi/files/"+documentId+"?access_token="+accessToken;
	}
	
	public static String GetWOPISrc(String documentId, String localHost) {
		// according to the documentation, there is not token_access in a WOPISrc URL
		// it should be URL encoded
		String nonEncodedURL="http://"+localHost+"/wopi/files/"+documentId;
		try {
			return URLEncoder.encode(nonEncodedURL, StandardCharsets.UTF_8.toString());
		} catch(UnsupportedEncodingException e) {
			logger.info("Error during URL encoding. Back to non encoded URL");
			logger.trace("Exception details",e);
			return nonEncodedURL;
		}
	}
	
	public static String getWholeURL(String documentId, String localHost, String endPoint) {
		if(endPoint!=null)
			return endPoint+"WOPISrc="+GetWOPISrc(documentId, localHost);
		else	// endpoint not parsed, just return the WOPISrc
			return "WOPISrc="+GetWOPISrc(documentId, localHost);
	}
	
	public static String buildHost(HttpServletRequest request) {
		return request.getLocalName()+":"+request.getLocalPort();
	}
}
