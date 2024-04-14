package org.wopiserver.configuration.itf;

public interface ConfigurationService {
	
	/** set configuration */
	public void setConfiguration(String baseDirectory);

	/** where to find the files */
	public String getBaseDirectory();

	/** generate configuration string */
	public String toString();
}
