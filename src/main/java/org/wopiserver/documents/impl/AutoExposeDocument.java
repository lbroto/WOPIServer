package org.wopiserver.documents.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.wopiserver.configuration.itf.ConfigurationService;
import org.wopiserver.controller.response.PutRelativeFileResponse;
import org.wopiserver.controller.response.PutRelativeFileResponse.PutRelativeFileResponseBuilder;
import org.wopiserver.documents.itf.DocumentService;
import org.wopiserver.documents.pojo.Document;
import org.wopiserver.exception.WOPIException;

import ch.qos.logback.classic.Logger;
import jakarta.annotation.PostConstruct;

public class AutoExposeDocument implements DocumentService {
	
	private Logger logger = (Logger)LoggerFactory.getLogger(this.getClass());
	
	private Map<String,Document> documentMap;
	private List<String> knownExtensions;

	@Autowired
	private ConfigurationService configurationService;
	/**
	 * 
	 * This method look at new file in the baseDir every minute. Every new file are added in the documentMap
	 * 
	 * If a file is modified => we update lastModifiedTime and size when the properties are asked
	 * 
	 * If a file is removed => we throw a 404 when properties or content are asked.
	 * 
	 */
	
	@Scheduled(fixedRate = 60000)
	private void updateBaseDirContent() {
		String baseDir=configurationService.getBaseDirectory();
		String filename, extension;
		
		if(configurationService==null || knownExtensions==null || documentMap==null || baseDir==null)
			return;		// bean is not yet managed - we can be called before PostConstruct
		
		logger.trace("Updating the content of "+baseDir);
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(baseDir))) {
			for (Path path : stream) {
				if (!Files.isDirectory(path)) {
					filename=path.getFileName().toString();
					extension=FilenameUtils.getExtension(filename);
					if(knownExtensions.contains(extension))
					{
						// create a document. The documentId is its name, baseFileName its name and owner = wopi
						if(!documentMap.containsKey(filename)) {
							Document d=new Document.DocumentBuilder(filename,filename,"wopi")
									.setUserId("wopi")
									.setUserCanWrite(true)
									.setUserFriendlyName("wopi")
									.build();
							
							documentMap.put(filename, d);
							logger.info("Added new file "+buildAbsolutePath(d.getBaseFileName()));
						}
					}
				}
			}
		} catch (IOException e) {
			logger.trace("Exception while updating "+baseDir+" content",e);
		}
	}
	
	@PostConstruct
	public void construct() {
		logger.info("Document automagically managed");
		documentMap=new Hashtable<String,Document>();
		knownExtensions=Arrays.asList("ods", "odt", "xls", "xlsx", "csv", "docx", "dotx", "doc", "xml", "dot", "ppt", "pptx", "odp", "odg");
		logger.info("The follwing files will be exposed: "+knownExtensions);
		updateBaseDirContent();
	}
	
	/********************************************************************************
	 * 																				*
	 * 						Exchanges with Collabora Online							*
	 * 																				*
	 ********************************************************************************/	

	@Override
	public Document getDocumentProperties(String documentId, String token) throws WOPIException {
		// ignoring token - documentId is the documentName in this implementation
		logger.info("Getting document ID: "+documentId+" properties");
		
		// retrieve the document from the Document Map
		SimpleEntry<Document,File> se=getExistingFile(documentId);
		Document d=se.getKey();
		File f=se.getValue();
		
		d.setSize(f.length());
		d.setLastModifiedTime(new Date(f.lastModified()));
			
		return d;
	}
	
	@Override
	public byte[] getContent(String documentId, String access_token) throws WOPIException {
		// ignoring token - documentId is the documentName in this implementation
		logger.info("Getting document "+documentId);
		
		// retrieve the document from the Document Map
		SimpleEntry<Document,File> se=getExistingFile(documentId);
		Document d=se.getKey();
		File f=se.getValue();
		
		try {
			return FileUtils.readFileToByteArray(f);
		} catch(IOException e) {
			throw new WOPIException(WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.READ, "document path: "+buildAbsolutePath(d.getBaseFileName()));			
		}
	}

	@Override
	public String replaceContent(String documentId, String access_token, byte[] content) throws WOPIException {
		logger.info("Updating content of "+documentId);
		
		SimpleEntry<Document,File> se=getExistingFile(documentId);
		File f_orig=se.getValue();
		File file_1=new File(configurationService.getBaseDirectory()+"/"+UUID.randomUUID());
		File file_2=new File(configurationService.getBaseDirectory()+"/"+UUID.randomUUID());
		
		// customer data => **valuable data**
		// two stages replacing data :
		// 1- create a file (file_1)
		// 2- read the new files and compare the streams
		// 3- if equals, rename the original to a 3rd file (f_orig => file_2)
		// 4- rename the file created to the original one (file_1 => file_orig)
		// 5- remove the original file which now file_2
		// if an operation fails, return to original situation and raise an exception
		
		try {
			// first create a file (file_1) with the provided content
			FileUtils.writeByteArrayToFile(file_1, content);
			
			// second, re-read the content of file_1 and compare against provided content
			byte[] newFileContent=FileUtils.readFileToByteArray(new File(file_1.getPath()));
			if(Arrays.equals(newFileContent, content)) {
				// arrays are the same, we can swap the files
				logger.trace("File is well written");
				
				// now rename f_orig => other file (file_2) -- if it fails, the original file will be still there ( ATOMIC_MOVE )
				try {
					FileUtils.moveFile(f_orig, file_2,StandardCopyOption.ATOMIC_MOVE);	
				} catch(Exception e) {
					// issue during renaming. Do nothing
					throw new WOPIException(e, WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.RENAME);
				} 
				
				// if we are still here, rename file_1 => file_orig
				try {
					FileUtils.moveFile(file_1, f_orig, StandardCopyOption.ATOMIC_MOVE);
				} catch(Exception e) {
					// error during renaming. Log the issue for the file name
					logger.error("Error during replacing content. The original file contents are in "+file_2.getName()+" and the new contents are in "+file_1.getName());
					throw new WOPIException(e, WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.RENAME);					
				}
				
				// Everything OK, removing file_2
				FileUtils.delete(file_2);
			}
			else {	// files are different, remove file_1 and then fails with exception
				FileUtils.delete(file_1);
				throw new WOPIException(WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.CREATE,"Unable to create file for content replacement");
			}
			
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(new Date(f_orig.lastModified()));

		} catch (IOException e) {
			throw new WOPIException(e, WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.MODIFY);
		}
	}

	@Override
	public PutRelativeFileResponse changeExtension(String documentId, String accessToken, String newExtension, String localHost, byte[] content) throws WOPIException {

		String newAccessToken=UUID.randomUUID().toString();
		
		// retrieve the document from the Document Map
		SimpleEntry<Document,File> se=getExistingFile(documentId);
		Document src=se.getKey();
		
		// find out a new working extension
		String fileNameWithNEWExtension=changeExtension(src.getBaseFileName(), newExtension);
		
		// here the file is already created on the FS. Create it in the documentMap
		Document d=new Document.DocumentBuilder(fileNameWithNEWExtension,fileNameWithNEWExtension,"wopi")
				.setUserId("wopi")
				.setUserCanWrite(true)
				.setUserFriendlyName("wopi")
				.build();
		
		documentMap.put(fileNameWithNEWExtension, d);
		
		// now, replace the content (empty file) by the right content
		replaceContent(fileNameWithNEWExtension, newAccessToken, content);
		
		// log
		logger.trace("Saving as "+buildAbsolutePath(documentId)+" -> "+buildAbsolutePath(fileNameWithNEWExtension));
		
		// build an answer for the client and return with it
		return new PutRelativeFileResponseBuilder(d.getBaseFileName(),createURL(d.getDocumentId(), newAccessToken, localHost)).build();
	}
	
	@Override
	public PutRelativeFileResponse saveAs(String documentId, String accessToken, String newFileName, String localHost, byte[] content) throws WOPIException {

		String newAccessToken=UUID.randomUUID().toString();

		// retrieve the document from the Document Map
		SimpleEntry<Document,File> se=getExistingFile(documentId);
		Document src=se.getKey();
		
		// find out a new working filename
		String newBaseFileNameWithPath=changeFilename(src.getBaseFileName(), newFileName);

		// here the file is already created on the FS. Create it in the documentMap
		Document d=new Document.DocumentBuilder(newBaseFileNameWithPath,newBaseFileNameWithPath,"wopi")
				.setUserId("wopi")
				.setUserCanWrite(true)
				.setUserFriendlyName("wopi")
				.build();
		
		documentMap.put(newBaseFileNameWithPath, d);
		
		// now, replace the content (empty file) by the right content
		replaceContent(newBaseFileNameWithPath, newAccessToken, content);

		// log
		logger.trace("Saving as "+buildAbsolutePath(documentId)+" -> "+buildAbsolutePath(newBaseFileNameWithPath));
		
		// build an answer for the client and return with it
		return new PutRelativeFileResponseBuilder(d.getBaseFileName(),createURL(d.getBaseFileName(), newAccessToken, localHost)).build();
	}

	/********************************************************************************
	 * 																				*
	 * 					Documents manipulation not the WOPI API						*
	 * 																				*
	 ********************************************************************************/
	
	@Override
	public List<String> listDocuments(String ourAddress, String localHost) throws WOPIException {
		List<String> url=new ArrayList<String>();
		logger.info("Sending the list of documents managed by this instance.");
		for(Document d: documentMap.values()) {
			try {
				url.add(URLEncoder.encode(createURL(d.getBaseFileName(), UUID.randomUUID().toString(), localHost), StandardCharsets.UTF_8.toString()));
			} catch (UnsupportedEncodingException e) {
				throw new WOPIException(e, WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.ENCODE);				
			}
		}
		return url;
	}
	
	/****************************************************************************
	 * 																			*
	 * 									Helpers									*
	 * 																			*
	 ****************************************************************************/

	private String createURL(String documentId, String accessToken, String localHost) {
		return "http://"+localHost+"/wopi/files/"+documentId+"?access_token="+accessToken;
	}
	
	private String buildAbsolutePath(String baseFileName) {
		return configurationService.getBaseDirectory()+"/"+baseFileName;
	}

	private SimpleEntry<Document,File> getExistingFile(String documentId) throws WOPIException {

		// do we manage this document ?
		Document d=documentMap.get(documentId);
		if(d==null)
			throw new WOPIException(WOPIException.Issue.NOT_FOUND, WOPIException.Type.DOCUMENT, WOPIException.Operation.LOOKUP, "document id: "+documentId);
		
		// yes, now get the properties and send them back		
		File f=new File(buildAbsolutePath(d.getBaseFileName()));

		// does this file still exist ?
		if(!f.exists()) {
			// nop, remove it for map and throw exception
			documentMap.remove(documentId);	// hashtable is synchronized so no issue with updateBaseDirContent
			throw new WOPIException(WOPIException.Issue.NOT_FOUND, WOPIException.Type.DOCUMENT, WOPIException.Operation.LOOKUP, "document path: "+buildAbsolutePath(d.getBaseFileName()));			
		}
		return new SimpleEntry<Document,File>(d,f);
	}
	
	/**
	 * 
	 * we must find a extension to get it work directly. eg: if the client pass us an extension which generate a conflict we must solve the conflict.   
	 * 
	 */	
	private String changeExtension(String baseFileName, String newExtension) throws WOPIException {
		String fileNameWithoutExtension=FilenameUtils.removeExtension(baseFileName);
		String fileNameWithNEWExtension=fileNameWithoutExtension+newExtension;
		int postExtension=1;

		// if the file already exists, we must change the name to be sure we can execute the operation
		while(new File(configurationService.getBaseDirectory()+"/"+fileNameWithNEWExtension).exists())
			fileNameWithNEWExtension=fileNameWithoutExtension+"-"+(postExtension++)+newExtension;
		
		// immediate creation of the file (empty)
		File f=new File(buildAbsolutePath(fileNameWithNEWExtension));
		try {
			f.createNewFile();
		} catch (IOException e) {
			throw new WOPIException(e, WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.CREATE);
		}
		
		logger.trace("new extension found => SRC: "+baseFileName+" to DST: "+fileNameWithNEWExtension);
		
		return fileNameWithNEWExtension;
	} 

	/**
	 * 
	 * we must find a filename to get it work directly. eg: if the client pass us a filename which generate a conflict we must solve the conflict.   
	 * 
	 */	
	public String changeFilename(String baseFileName, String newFilename) throws WOPIException {
		
		// break down the new file name with base + '.' + extension
		String fileNameWithoutExtension=FilenameUtils.removeExtension(newFilename);
		String fileExtension=FilenameUtils.getExtension(newFilename);
		
		// now re-create the new file name to test its existence
		String fileNameWithNEWName=fileNameWithoutExtension+"."+fileExtension;
		
		int postExtension=1;

		// if the file already exists, we must change the name to be sure we can execute the operation
		while(new File(buildAbsolutePath(fileNameWithNEWName)).exists())
			fileNameWithNEWName=fileNameWithoutExtension+"-"+(postExtension++)+"."+fileExtension;
		
		// immediate creation of the file (empty)
		File f=new File(buildAbsolutePath(fileNameWithNEWName));
		try {
			f.createNewFile();
		} catch (IOException e) {
			throw new WOPIException(e, WOPIException.Issue.ACCESS_ERROR, WOPIException.Type.DOCUMENT, WOPIException.Operation.CREATE);
		}
		
		logger.trace("new filename found => SRC: "+baseFileName+" to DST: "+fileNameWithNEWName);
		
		return fileNameWithNEWName;
	}
}
