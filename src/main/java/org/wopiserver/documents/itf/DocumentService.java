package org.wopiserver.documents.itf;

import org.wopiserver.controller.response.PutRelativeFileResponse;
import org.wopiserver.documents.pojo.Document;
import org.wopiserver.exception.WOPIException;

public interface DocumentService {
	
	/** Send document properties to the client */
	public Document getDocumentProperties(String documentId, String token) throws WOPIException;

	/** Replace document content */
	public String replaceContent(String documentId, String access_token, byte[] content) throws WOPIException;
	
	/** Send back document content */
	byte[] getContent(String documentId, String access_token) throws WOPIException;

	/** Change document extension */
	public PutRelativeFileResponse changeExtension(String documentId, String accessToken, String newExtension, String localHost, byte[] content) throws WOPIException;

	/** Save as document */
	public PutRelativeFileResponse saveAs(String documentId, String accessToken, String newFileName, String localHost, byte[] content) throws WOPIException;
	
	/** Print out every document handlded by this server */
	public void listDocuments() throws WOPIException;
}
