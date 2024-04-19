package org.wopiserver.configuration.impl;

import java.net.URI;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.wopiserver.configuration.itf.ConfigurationService;
import org.wopiserver.configuration.pojo.Configuration;

import ch.qos.logback.classic.Logger;

@Service
public class InMemoryConfigurationImpl implements ConfigurationService {
	
	private Configuration conf;
	private boolean codeURLValid=false;
	private boolean proxyHostValid=false;
	
	private Logger logger = (Logger)LoggerFactory.getLogger(this.getClass());
	
	public InMemoryConfigurationImpl() {
		conf=new Configuration();
	}

	@Override
	public String getBaseDirectory() {
		return conf.getBaseDir();
	}

	@Override
	public String toString() {
		String str="Configuration => ( Base Directory: "+getBaseDirectory()+" )";
		return str;
	}

	@Override
	public void setConfiguration(String baseDirectory) {
		conf.setBaseDir(baseDirectory);		
		logger.info(toString());
	}

	@Override
	public String getCodeURL() {
		if(!codeURLValid)
			return null;
		else
			return conf.getCodeURL();
	}
	
	@Override
	public void setCodeURL(String codeURL) {
		// try to connect to the URL
		// retrieve forever 
		new Thread( () -> {
			boolean interrupted=false;
			while(!interrupted) {
				try {	
					URI u=new URI(codeURL);
					URLConnection connection=u.toURL().openConnection();
					connection.connect();
					
					if(!codeURLValid) {	// we was not connected
						logger.info("Connection to CODE OK ");
						codeURLValid=true;
						conf.setCodeURL(codeURL);
					
						// update the endpoints
						scanEndPoints();
					}
				} catch (Exception e) {			
					logger.info("Unable to connect to "+codeURL);
					logger.trace("Exception details",e);
					codeURLValid=false;
				}	
				// try in 30s
				try {
					Thread.sleep(30000);
				} catch(InterruptedException e) {
					interrupted=true;
				}
			}
		}).start();
	}

	@Override
	public boolean isCodeURLValid() {
		return codeURLValid;
	}
	
	@Override
	public String getEndPoint(String documentExtension) {
		return conf.getEndPoints().get(documentExtension);
	}

	@Override
	public void setProxyHost(String proxyHost) {
		logger.info("Registering the reverse proxy host: "+proxyHost);
		conf.setProxyHost(proxyHost);
		proxyHostValid=true;
	}

	@Override
	public void scanEndPoints() {		
		// the Code URL is not set
		if(!isCodeURLValid())
			return;

		try {
			// mainly XML parsing			
			String ext,url,exUrl;
			UriComponentsBuilder replaceProxy;
			URI u=new URI(conf.getCodeURL()+"/hosting/discovery");
			URLConnection connection=u.toURL().openConnection();
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(connection.getInputStream());
			while (reader.hasNext()) {
			    XMLEvent nextEvent = reader.nextEvent();
			    if (nextEvent.isStartElement()) {
			        StartElement startElement = nextEvent.asStartElement();
			        if(startElement.getName().getLocalPart().equals("action")) {
		                Attribute extension=startElement.getAttributeByName(new QName("ext"));
		                Attribute urlsrc=startElement.getAttributeByName(new QName("urlsrc"));
		                ext=extension.getValue();
		                url=urlsrc.getValue();
		                if(!ext.isBlank() && !ext.isEmpty() && !url.isBlank() && !url.isEmpty()) {
		                	// dealing with endpoints update ... just in case of deco and reco to CODE, to avoid to add same the endpoints when scanning endpoints
		                	exUrl=conf.getEndPoints().get(ext);
		                	if(exUrl==null || !exUrl.equals(url)) {
		                		logger.info("add / update new endpoint: "+ext+" => "+url);
		                		if(proxyHostValid) {
		                			// replace the host in the URL
		                			replaceProxy=UriComponentsBuilder.fromHttpUrl(url);
		                			url=replaceProxy.host(conf.getProxyHost()).toUriString();
		                			logger.info("Replacing with reverse proxy => "+url);
		                		}
		                		if(url.charAt(url.length()-1)!='?')
		                			url += "?";
		                		conf.getEndPoints().put(ext, url);
		                	}
		                }
			        }
			    }
			}
		
		} catch (Exception e) {
			logger.info("Unable to connect to "+conf.getCodeURL());
			logger.trace("Exception details",e);
			codeURLValid=false;
		}	
	}

	@Override
	public void disableTLSCheck() {
		try {
			// accept 
			TrustManager[] trustAllCerts = new TrustManager[] { 
				    new X509TrustManager() {     
				        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
				            return null;
				        } 
				        public void checkClientTrusted( 
				            java.security.cert.X509Certificate[] certs, String authType) {
				            } 
				        public void checkServerTrusted( 
				            java.security.cert.X509Certificate[] certs, String authType) {
				        }
				    } 
				}; 
		    SSLContext sc = SSLContext.getInstance("SSL"); 
		    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		    HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        logger.error("**** Disable TLS check -- NOT RECOMMENDED ****");
		} catch(Exception e) {
			logger.info("Unable to disable TLS checks");
			logger.trace("Exception details",e);
		}
		
	}
}
