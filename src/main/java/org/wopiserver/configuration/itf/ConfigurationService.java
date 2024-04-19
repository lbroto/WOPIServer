package org.wopiserver.configuration.itf;

public interface ConfigurationService {
	
	/** set configuration */
	public void setConfiguration(String baseDirectory);
	
	/** disable TLS check */
	public void disableTLSCheck();

	/** where to find the files */
	public String getBaseDirectory();

	/** generate configuration string */
	public String toString();
	
	/** where to find CODE */
	public String getCodeURL();
	public void setCodeURL(String codeURL);
	public boolean isCodeURLValid();
	
	/** CODE Endpoint management */
	public void scanEndPoints();
	public String getEndPoint(String documentExtension);
	public void setProxyHost(String proxyHost);
}
