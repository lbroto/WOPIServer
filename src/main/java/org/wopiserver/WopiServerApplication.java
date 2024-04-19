package org.wopiserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.wopiserver.configuration.itf.ConfigurationService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class WopiServerApplication {

	@Autowired
	private ConfigurationService configurationService;
	
	// to be passed to the instance in the container
	private static HashMap<String,String> argsMap;
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(WopiServerApplication.class.getName());
	
	private static enum InstanceMode
	{ 
		SERVER_AUTO_EXPOSITION("[ SERVER ][ AUTO EXPOSITION ]"), CLI_DOCUMENT("[ CLI ][ DOCUMENT ]");
		private final String value;
		private InstanceMode(String value) {
			this.value = value;	
		}
		public String toString() {
			return value;
		}
	};
	private static InstanceMode instanceMode; 		// only one mode possible per instance
	private static boolean exit=false;				// helper to exit if parsing error

	public static void main(String[] args) {

		/* setting the logging level */
		String logLevelRequested=System.getProperty("logging.level.org.wopiserver");
		if(logLevelRequested==null)
			logLevelRequested="INFO";
		
		logger.setLevel(Level.toLevel(logLevelRequested));
		
		logArgs(args);

		/* parsing the command line arguments, set the mode and storing the arguments */
		argsMap=new HashMap<String,String>();

		Options options = new Options();
		
		// all modes
		options.addOption("b", "baseDir", true, "[ ALL MODES ] The directory where the files to be served are located");
		options.addOption("c", "codeURL", true, "[ ALL MODES ] The Collabora Online URL. Example: --codeURL https://localhost:9980");
		options.addOption("p", "proxyHost", true, "[ ALL MODES ] Reverse proxy address. Example: --proxyHost 192.168.1.254");
		options.addOption("k", "disableTLSCheck", false, "[ ALL MODES ] Disable SSL/TLS checks. Not recommanded.");

		// documents mode
		//options.addOption("L", "listDocuments", false, "[ CLI - MODE DOCUMENTS ] List all documents managed by this WOPI instance");

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(options, args);

			// mode detection
			if(line.hasOption("file") || line.hasOption("document") || line.hasOption("listDocuments")) {
				instanceMode=InstanceMode.CLI_DOCUMENT;
			}
			else {
				instanceMode=InstanceMode.SERVER_AUTO_EXPOSITION;
			}
			
			logger.info("Mode "+instanceMode+" detected");
			
			parseAllModes(line);
			
			// looking for the arguments for each mode
			switch(instanceMode) {
				case InstanceMode.SERVER_AUTO_EXPOSITION:
					parseServerMode(line);
					break;
				case InstanceMode.CLI_DOCUMENT:
					parseDocumentMode(line);
					break;					
			}
		}
		catch(ParseException e) {			
			// just to exit and print out help
			exit=true;
		}
	
		// now start the application and leave the static domain
		if(exit) {
			printHelp(args, options);
		}
		else {
			
			switch(instanceMode) {
				case InstanceMode.SERVER_AUTO_EXPOSITION:
					logger.warn("Starting "+instanceMode);		// start with Tomcat
					new SpringApplicationBuilder(WopiServerApplication.class).logStartupInfo(true).bannerMode(Mode.OFF).run(args);
					break;
				case InstanceMode.CLI_DOCUMENT:
					logger.warn("Starting "+instanceMode);		// start without Tomcat
					new SpringApplicationBuilder(WopiServerApplication.class).web(WebApplicationType.NONE) .logStartupInfo(false).bannerMode(Mode.OFF).run(args);
					break;					
			}		
		} 
	}

	@PostConstruct
	public void construct() {
		
		// now all the beans and especially the configuration beans are ready
		// set the configuration
		
		configurationService.setConfiguration(argsMap.get("baseDir"));
		
		// process the optional args
		if(argsMap.containsKey("disableTLSCheck"))
			configurationService.disableTLSCheck();
		
		if(argsMap.containsKey("proxyHost")) {
			configurationService.setProxyHost(argsMap.get("proxyHost"));
		}
		
		if(argsMap.containsKey("codeURL")) {
			configurationService.setCodeURL(argsMap.get("codeURL"));
		}
	//	try {
			switch(instanceMode) {
				case InstanceMode.SERVER_AUTO_EXPOSITION:
					logger.warn("Started WOPI "+instanceMode);
					break;
					
				case InstanceMode.CLI_DOCUMENT:				
					logger.warn("Started WOPI "+instanceMode);
					break;	
					
				}
			
	//		} catch(WOPIException e) {
	//		e.logException();
	//	}		
	}
	
	private static void parseAllModes(CommandLine line) {
		logger.trace("Looking for "+instanceMode+" general parameters");
		
		// mandatory
		if(line.hasOption("baseDir")) {	
			argsMap.put("baseDir",line.getOptionValue("baseDir"));
		} else {
			exit=true;
		}

		// optional
		if(line.hasOption("proxyHost")) {
			argsMap.put("proxyHost",line.getOptionValue("proxyHost"));
		}
		
		if(line.hasOption("codeURL")) {
			argsMap.put("codeURL",line.getOptionValue("codeURL"));
		}
		
		if(line.hasOption("disableTLSCheck")) {
			argsMap.put("disableTLSCheck",null);
		}

	}
	
	private static void parseServerMode(CommandLine line) {
		logger.trace("Looking for "+instanceMode+" mode parameters");

	}
	
	private static void parseDocumentMode(CommandLine line) {
		logger.trace("Looking for "+instanceMode+" mode parameters");
		
		// optional
		if(line.hasOption("listDocuments")) {
			argsMap.put("listDocuments", "listDocuments");
		}

	}
	
	private static void logArgs(String[] args) {
		logger.trace("Received args:");
		
		for(String s:args) {
			logger.trace("==> "+s);
		}

	}
	
	private static void printHelp(String[] args, Options options) {
		// we encountered an error parsing the command line. We print out the help whatever the log level is.
		logger.error("Command line parsing error");
		String header=	"\n\n+-----------------------------+\n" +
							"| Commande line parsing error |\n" +
							"+-----------------------------+\n\n" +
						"Received args:\n";
		
		System.err.println(header);
		
		for(String s:args) {
			System.err.println("==> "+s);
		}

		String middle=	"\n\n+----------------------------------------+\n" +
							"| Program help: args and functionalities |\n" +
							"+----------------------------------------+\n\n";
		
		System.err.println(middle);
		
		HelpFormatter formatter = new HelpFormatter();
		StringWriter out=new StringWriter();
	    PrintWriter pw=new PrintWriter(out);

	    formatter.printHelp(pw, 150, "WOPIServerApplication", "", options, formatter.getLeftPadding(), formatter.getDescPadding(), "", true);
	    pw.flush();

	    System.err.println(out.toString());
		
		System.err.println();
		String line=
				"\nThis app can be launched in multiple modes: \n\n"+
						"+---------------------------------------------------------------------+\n" +
						"| SERVER MODE: this mode permits to communicate with Collabora Online |\n" +
						"+---------------------------------------------------------------------+\n\n" +
						"************************** SUB-MODE AUTO EXPOSITION ************************\n\n" +
						"This mode is selected when no DB file (--dbFile) parameter is passed. \n" +
						"In this mode, the baseDir is scanned every minutes and all the documents contained in baseDir are exposed.\n" +
						"You can pass the --codeurl parameter to parametrize the URLs with the CODE endpoints.\n" +
						"The reverse proxy permits to subsitute the CODE endpoint with the reverse proxy address .\n" +						
						"The access_token is ignored in this mode.\n\n" +
						"Example: java -jar WopiServerApplication.jar --baseDir /Users/laurent/tmp \n\n" +
/*						"\n+---------------------------------------------------------------------------------------------+\n" +
						  "| CLI MODE: theses modes permits to interact with WOPI (started or not depending on the mode) |\n" +
						  "+---------------------------------------------------------------------------------------------+\n\n" +
						"All these mode required the parameter baseDir.\n\n" +
						"************************** SUB-MODE DOCUMENT ************************\n\n" +
						"This mode permits to manage the documents handled by this WOPI instance. Only listDocuments is supported at this time.\n" +
						"If the WOPI Server is started in SUB-MODE AUTO EXPOSITION, it must be started to list the documents managed\n" +
						"Example: java -jar WopiServerApplication.jar --baseDir /Users/laurent/tmp --listDocuments\n\n" +*/
						"\n+-------------------+\n" +
						  "| DEBUGGING OPTIONS |\n" +
						  "+-------------------+\n\n" +
						"The debugging level is set to INFO by default. It can be changed by passing arguments to the JVM:\n" +
						"- for the Spring container: -Dlogging.level.root=ERROR \n" +
						"- for the WOPI instance: -Dlogging.level.org.wopiserver=TRACE\n"+
						"\n\nThe levels can be OFF > ERROR > WARN > INFO > DEBUG > TRACE\n\n" +
						"\n+----------------+\n" +
						  "| LISTENING PORT |\n" +
						  "+----------------+ \n\n" +
						"To change the listening port, add -Dserver.port=<port> to the command line\n";

		System.err.println(line);
	}
}
