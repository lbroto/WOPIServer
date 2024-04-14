package org.wopiserver.documents.impl;

import org.wopiserver.controller.response.PutRelativeFileResponse;
import org.wopiserver.documents.itf.DocumentService;
import org.wopiserver.documents.pojo.Document;
import org.wopiserver.exception.WOPIException;

public class AutoExposeDocument implements DocumentService {

	@Override
	public Document getDocumentProperties(String documentId, String token) throws WOPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String replaceContent(String documentId, String access_token, byte[] content) throws WOPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getContent(String documentId, String access_token) throws WOPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PutRelativeFileResponse changeExtension(String documentId, String accessToken, String newExtension,
			String localHost, byte[] content) throws WOPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PutRelativeFileResponse saveAs(String documentId, String accessToken, String newFileName, String localHost,
			byte[] content) throws WOPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void listDocuments() throws WOPIException {
		// TODO Auto-generated method stub
		
	}
}
