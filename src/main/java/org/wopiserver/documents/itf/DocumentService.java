package org.wopiserver.documents.itf;

import java.util.Collection;

import org.wopiserver.controller.response.PutRelativeFileResponse;
import org.wopiserver.documents.pojo.Document;
import org.wopiserver.exception.WOPIException;

public interface DocumentService {
	
	/** Send document properties to the client */
	public Document getDocumentProperties(String documentId, String token) throws WOPIException;

	/** Send back document content */
	byte[] getContent(String documentId, String access_token) throws WOPIException;

	/** Replace document content */
	public String replaceContent(String documentId, String access_token, byte[] content) throws WOPIException;
	
	/** Change document extension */
	public PutRelativeFileResponse changeExtension(String documentId, String accessToken, String newExtension, String localHost, byte[] content) throws WOPIException;

	/** Save as document */
	public PutRelativeFileResponse saveAs(String documentId, String accessToken, String newFileName, String localHost, byte[] content) throws WOPIException;
	
	/** Retrieve every document URL handled by this server */
	public Collection<String> listDocumentsURL(String ourAddress, String clientAddress) throws WOPIException;
	
	/** Retrieve every document handled by this server */
	public Collection<Document> listDocuments() throws WOPIException;

	/** Retrieve document extension (to publish on the right endpoint */
	public String getDocumentExtention(Document d);
}
