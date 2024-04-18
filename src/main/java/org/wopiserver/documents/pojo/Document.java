package org.wopiserver.documents.pojo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {

	/** WOPI Server metadata */
	@JsonIgnore private String documentId;
	
	/** Collabora Online WOPI Minimum set of properties */
	@JsonProperty("BaseFileName") 
	private String baseFileName;					// immutable after creation
	
	@JsonProperty("OwnerId") 
	private String ownerId;							// immutable after creation
	
	@JsonProperty("Size") 
	private long size;								// updated time needed
	
	@JsonProperty("UserId") 
	private String userId;							// depends of the granting
	
	/** Collabora Online WOPI Optional fields */
	@JsonProperty("UserCanWrite") 
	private boolean userCanWrite;					// immutable after adding a permission
	
	@JsonProperty("UserFriendlyName") 
	private String userFriendlyName;				// depends if provided or not
	
	@JsonProperty("LastModifiedTime") 
	private Date lastModifiedTime;					// depends on FS time

	/** immutable values */
	@JsonProperty("HidePrintOption") 
	private boolean hidePrintOption;				// always false
	
	@JsonProperty("DisablePrint") 
	private boolean disablePrint;					// always false
	
	@JsonProperty("HideSaveOption") 
	private boolean hideSaveOption;					// always false
	
	@JsonProperty("HideExportOption") 
	private boolean hideExportOption;				// always false
	
	@JsonProperty("DisableExport") 
	private boolean disableExport;					// always false
	
	@JsonProperty("DisableCopy") 
	private boolean disableCopy;					// always false
	
	@JsonProperty("EnableOwnerTermination") 
	private boolean enableOwnerTermination;			// always true
	
	@JsonProperty("IsUserLocked") 
	private boolean isUserLocked;					// always false
	
	@JsonProperty("IsUserRestricted") 
	private boolean isUserRestricted;				// always false
	
	@JsonProperty("UserCanNotWriteRelative") 
	private boolean userCanNotWriteRelative;		// always true
	
	@JsonProperty("SupportsUpdate") 
	private boolean supportsUpdate;					// always true
	
	@Override
	public String toString() {
		String str=documentId+";"+baseFileName+";"+ownerId;
		return str;
	}
	
	public boolean isHidePrintOption() {
		return hidePrintOption;
	}
	public void setHidePrintOption(boolean hidePrintOption) {
		this.hidePrintOption = hidePrintOption;
	}
	public boolean isDisablePrint() {
		return disablePrint;
	}
	public void setDisablePrint(boolean disablePrint) {
		this.disablePrint = disablePrint;
	}
	public boolean isHideSaveOption() {
		return hideSaveOption;
	}
	public void setHideSaveOption(boolean hideSaveOption) {
		this.hideSaveOption = hideSaveOption;
	}
	public boolean isHideExportOption() {
		return hideExportOption;
	}
	public void setHideExportOption(boolean hideExportOption) {
		this.hideExportOption = hideExportOption;
	}
	public boolean isDisableExport() {
		return disableExport;
	}
	public void setDisableExport(boolean disableExport) {
		this.disableExport = disableExport;
	}
	public boolean isDisableCopy() {
		return disableCopy;
	}
	public void setDisableCopy(boolean disableCopy) {
		this.disableCopy = disableCopy;
	}
	public boolean isEnableOwnerTermination() {
		return enableOwnerTermination;
	}
	public void setEnableOwnerTermination(boolean enableOwnerTermination) {
		this.enableOwnerTermination = enableOwnerTermination;
	}
	public String getLastModifiedTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		return dateFormat.format(lastModifiedTime);
	}
	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	public boolean isIsUserLocked() {
		return isUserLocked;
	}
	public void setIsUserLocked(boolean isUserLocked) {
		this.isUserLocked = isUserLocked;
	}
	public boolean isIsUserRestricted() {
		return isUserRestricted;
	}
	public void setIsUserRestricted(boolean isUserRestricted) {
		this.isUserRestricted = isUserRestricted;
	}
	public String getUserFriendlyName() {
		return userFriendlyName;
	}
	public void setUserFriendlyName(String userFriendlyName) {
		this.userFriendlyName = userFriendlyName;
	}
	public boolean isUserCanWrite() {
		return userCanWrite;
	}
	public void setUserCanWrite(boolean userCanWrite) {
		this.userCanWrite = userCanWrite;
	}
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String fileId) {
		this.documentId = fileId;
	}
	public String getBaseFileName() {
		return baseFileName;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isUserCanNotWriteRelative() {
		return userCanNotWriteRelative;
	}
	public void setUserCanNotWriteRelative(boolean userCanNotWriteRelative) {
		this.userCanNotWriteRelative = userCanNotWriteRelative;
	}

	public boolean isSupportsUpdate() {
		return supportsUpdate;
	}

	public void setSupportsUpdate(boolean supportsUpdate) {
		this.supportsUpdate = supportsUpdate;
	}

	private Document(DocumentBuilder d) {
		
		documentId=d.documentId;
		baseFileName=d.baseFileName;
		ownerId=d.ownerId;
		size=d.size;
		userId=d.userId;
		userCanWrite=d.userCanWrite;
		userFriendlyName=d.userFriendlyName;
		lastModifiedTime=d.lastModifiedTime;

		// immutable properties
		setHidePrintOption(false);
		setDisablePrint(false);
		setHideSaveOption(false);
		setHideExportOption(false);
		setDisableExport(false);
		setDisableCopy(false);
		setEnableOwnerTermination(true);
		setIsUserLocked(false);
		setIsUserRestricted(false);
		setUserCanNotWriteRelative(false);
		setSupportsUpdate(true);
	}
	
	public static class DocumentBuilder {
		
		public DocumentBuilder(String documentId, String baseFileName, String ownerId) {
			
			// mandatory
			this.documentId=documentId;
			this.baseFileName=baseFileName;
			this.ownerId=ownerId;
		}
		
		public Document build() {
			return new Document(this);
		}
		private String documentId;
		private String baseFileName;					// immutable after creation
		private String ownerId;							// immutable after creation
		private long size;								// read each time needed
		private String userId;							// depends of the granting
		private boolean userCanWrite;					// immutable after adding a permission
		private String userFriendlyName;				// depends if provided or not
		private Date lastModifiedTime;					// depends on FS time
		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}
		public DocumentBuilder setBaseFileName(String baseFileName) {
			this.baseFileName = baseFileName;
			return this;
		}
		public DocumentBuilder setOwnerId(String ownerId) {
			this.ownerId = ownerId;
			return this;
		}
		public DocumentBuilder setSize(long size) {
			this.size = size;
			return this;
		}
		public DocumentBuilder setUserId(String userId) {
			this.userId = userId;
			return this;
		}
		public DocumentBuilder setUserCanWrite(boolean userCanWrite) {
			this.userCanWrite = userCanWrite;
			return this;
		}
		public DocumentBuilder setUserFriendlyName(String userFriendlyName) {
			this.userFriendlyName = userFriendlyName;
			return this;
		}
		public DocumentBuilder setLastModifiedTime(Date lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
			return this;
		}
	}
}
