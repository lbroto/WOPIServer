package org.wopiserver.controller;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.wopiserver.configuration.itf.ConfigurationService;
import org.wopiserver.documents.itf.DocumentService;
import org.wopiserver.documents.pojo.Document;
import org.wopiserver.exception.WOPIException;
import org.wopiserver.helper.URLHelpers;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DocumentService documentInterface;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping("/")
	public String ShowDocuments(HttpServletRequest request, ModelMap model) {
		logger.warn("Generating HTML tab for documents");
		try {
			model.addAttribute("list", findAllDocument(request));
			return "documentList";			
		} catch(WOPIException e) {
			e.logException();
			throw new ResponseStatusException(e.getHttpStatus());
		}
	}
	
	@GetMapping(value="/update-list-doc")
	public String updateListInstance(HttpServletRequest request, ModelMap model) {
		logger.trace("Updating HTML fragment for documents");
		try {
			model.addAttribute("list", findAllDocument(request));
			return "documentList :: #updateListDoc";
		} catch(WOPIException e) {
			e.logException();
			throw new ResponseStatusException(e.getHttpStatus());
		}		
	}
	
	private Collection<SimpleEntry<Document,String>> findAllDocument(HttpServletRequest request) throws WOPIException {

		Collection<Document> list=documentInterface.listDocuments();
		Collection<SimpleEntry<Document,String>> listForThymeleaf=new ArrayList<SimpleEntry<Document,String>>();
		
		String url=UUID.randomUUID().toString();
		String localHost=URLHelpers.buildHost(request);
		
		for(Document d: list) {
			url=URLHelpers.getWholeURL(d.getDocumentId().toString(),localHost, configurationService.getEndPoint(documentInterface.getDocumentExtention(d)));
			listForThymeleaf.add(new SimpleEntry<Document,String>(d,url));
		}
		return listForThymeleaf;
	}
}
