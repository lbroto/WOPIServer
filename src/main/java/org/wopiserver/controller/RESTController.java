package org.wopiserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.wopiserver.controller.response.PutFileResponse;
import org.wopiserver.controller.response.PutRelativeFileResponse;
import org.wopiserver.documents.itf.DocumentService;
import org.wopiserver.documents.pojo.Document;
import org.wopiserver.exception.WOPIException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * We don't deal with X-WOPI-xxx because all of these headers are almost for logging purpose but we do not log any requests.
 * We will handle the following headers: 
 * 	- Content-type which is a HTTP standard header
 *  - X-WOPI-SuggestedTarget pour le putRelativeFile
 *  - Authorization: we do not use (yet) any authorization mechanism to authenticate WOPI client
 *  
 */

@RestController
public class RESTController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DocumentService documentInterface;
	
	@Autowired
	private ObjectMapper mapper;
	
	/** 
	 * 
	 * Request for document properties 
	 * 
	 */
	@GetMapping("/wopi/files/{documentId}")
	public ResponseEntity<Document> checkFileInfo(@PathVariable String documentId, @RequestParam(name="access_token") String accessToken) {
		logger.warn("Request information on file with id "+documentId);
		try {
			Document d=documentInterface.getDocumentProperties(documentId, accessToken);
			
			// log the response if in trace level
			try {
				logger.trace(mapper.writeValueAsString(d));
			} catch (JsonProcessingException e) {
				logger.trace("Unable to log response", e);
			}
			return new ResponseEntity<Document>(d, HttpStatus.OK);
		} catch (WOPIException e) {
			e.logException();
			throw new ResponseStatusException(e.getHttpStatus());
		} 
	}
	
	/** 
	 * 
	 * Request for save as or extension change
	 * 
	 * COOL provides only X-WOPI-SuggestedTarget => we just deal with it for now 
	 * 
	 */
	@PostMapping(value="/wopi/files/{documentId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<PutRelativeFileResponse> putRelativeFile( HttpServletRequest request,
			@RequestHeader("X-WOPI-SuggestedTarget") String newPath, 
			@PathVariable String documentId, 
			@RequestParam(name="access_token") String accessToken, 
			@RequestBody byte[] content) {
		logger.warn("Save document "+documentId+" *AS* "+newPath);
		
		try {
			PutRelativeFileResponse putRelativeFileResponse;
			
			// determination of the action to perform
			if(newPath.startsWith("."))	{	// extension modification
				logger.trace("extension change to "+newPath);
				putRelativeFileResponse=documentInterface.changeExtension(documentId, accessToken, newPath, buildHost(request), content);
				
			} else {						//it's a full file name
				logger.trace("file name change to "+newPath+" if possible (otherwise we will found a suitable name)");
				putRelativeFileResponse=documentInterface.saveAs(documentId, accessToken, newPath, buildHost(request), content);
			}

			try {
				logger.trace("Answering:"+mapper.writeValueAsString(putRelativeFileResponse));
			} catch (JsonProcessingException e) {
				logger.trace("Unable to log response", e);
			}
			
			return new ResponseEntity<PutRelativeFileResponse>(putRelativeFileResponse, HttpStatus.OK);
		} catch(WOPIException e) {
			e.logException();
			throw new ResponseStatusException(e.getHttpStatus());			
		}
	}
	
	/** 
	 *  
	 * Request for document content update
	 * 
	 */
	@PostMapping(value="/wopi/files/{documentId}/contents", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<PutFileResponse> putFile(@PathVariable String documentId, @RequestParam(name="access_token") String accessToken, @RequestBody byte[] content) {
		logger.warn("Update content of the file "+documentId);
		try {
			logger.trace("Getting "+content.length+" bytes");
			String lastModifiedTime=documentInterface.replaceContent(documentId, accessToken, content);
			PutFileResponse putFileResponse=new PutFileResponse.PutFileResponseBuilder(lastModifiedTime).build();
			try {
				logger.trace("Answering:"+mapper.writeValueAsString(putFileResponse));
			} catch (JsonProcessingException e) {
				logger.trace("Unable to log response", e);
			}
			return new ResponseEntity<PutFileResponse>(putFileResponse, HttpStatus.OK);
		} catch(WOPIException e) {
			e.logException();
			throw new ResponseStatusException(e.getHttpStatus());			
		}
	}
	
	/**
	 * 
	 * Request for document content
	 * 
	 */
	@GetMapping(value="/wopi/files/{documentId}/contents", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> getFile(@PathVariable String documentId, @RequestParam(name="access_token") String accessToken) {
		logger.warn("Request contents of file with id "+documentId);
		try {
			byte [] responseByteArray=documentInterface.getContent(documentId, accessToken);
			// log the response if in trace level
			logger.trace("returning "+responseByteArray.length+" bytes");
			return new  ResponseEntity<byte[]>(responseByteArray, HttpStatus.OK);
		} catch(WOPIException e) {
			e.logException();
			throw new ResponseStatusException(e.getHttpStatus());
		}
	}
	
	private String buildHost(HttpServletRequest request) {
		return request.getLocalName()+":"+request.getLocalPort();
	}
}
