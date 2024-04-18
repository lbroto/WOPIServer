package org.wopiserver.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class WOPIException extends Throwable {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long serialVersionUID = 7539463097251591829L;
	public static enum Issue {NOT_FOUND, FORBIDDEN, ACCESS_ERROR};
	public static enum Type{DOCUMENT};
	public static enum Operation{
		GET_META_DATA("GETTING META DATA"), READ("READING"),WRITE("WRITING"),CREATE("CREATING"),MODIFY("MODIFYING"),LOOKUP("SEARCHING"),LIST("LISTING"),COPY("COPYING"), REPLACE("REPLACING"), RENAME("RENAMING"), ENCODE("ENCODING");
		private final String valueToDisplay;
		private Operation(String valueToDisplay) {
			this.valueToDisplay = valueToDisplay;	
		}
		public String getValueToDisplay() {
			return valueToDisplay;
		}
	};

	private Issue issue;
	private Type type;
	private Operation operation;
	private Throwable originalException;
	
	/** This field permit us to know if we are raised from an Exception or from scratch */
	private boolean wasAnException;
	
	public WOPIException(Issue issue, Type type, Operation operation) {
		super();
		this.issue=issue;
		this.type=type;
		this.operation=operation;
		wasAnException=false;
	}	
	
	public WOPIException(Issue issue, Type type, Operation operation, String message) {
		super(message);
		this.issue=issue;
		this.type=type;
		this.operation=operation;
		wasAnException=false;
	}

	
	public WOPIException(Throwable t, Issue issue, Type type, Operation operation) {
		super(t);
		this.issue=issue;
		this.type=type;
		this.operation=operation;
		wasAnException=true;
		this.originalException=t;
	}

	public Issue getIssue() {
		return issue;
	}
	
	public Type getType() {
		return type;
	}
	
	public Operation getOperation() {
		return operation;
	}

	public boolean wasAnException() {
		return wasAnException;
	}
	
	public String printOrigin() {
		String str="Error when "+operation+" a "+type+": "+issue;
		return str;
	}
	
	public void logException() {
		logger.warn(printOrigin());
		if(wasAnException) {
			logger.info(originalException.toString());			
		} else {
			logger.info(getMessage());
		}
		logger.trace("Stack Trace:", this);
	}
	
	public HttpStatus getHttpStatus() {
		switch(issue) {
		case Issue.NOT_FOUND:
			return HttpStatus.NOT_FOUND;				// 404	
		case Issue.FORBIDDEN:
			return HttpStatus.UNAUTHORIZED;				// 401
		case Issue.ACCESS_ERROR:
			default:
			return HttpStatus.INTERNAL_SERVER_ERROR;	// 500
		}
	}
}
