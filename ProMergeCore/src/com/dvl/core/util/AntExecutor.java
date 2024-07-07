package com.dvl.core.util;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * @author srccodes.com
 * @version 1.0
 */
public class AntExecutor {
	/**
	 * To execute the default target specified in the Ant build.xml file
	 * 
	 * @param buildXmlFileFullPath
	 */
	public static boolean executeAntTask(String buildXmlFileFullPath) {
		return executeAntTask(buildXmlFileFullPath, null);
	}

	/**
	 * To execute a target specified in the Ant build.xml file
	 * 
	 * @param buildXmlFileFullPath
	 * @param target
	 */
	public static boolean executeAntTask(String buildXmlFileFullPath,
										 String target) {
		//Aqui tem a configuração do retorno do Log da compilação
		DefaultLogger consoleLogger = getConsoleLogger();

		// Prepare Ant project
		Project project = new Project();
		File buildFile = new File(buildXmlFileFullPath);
		
		project.setUserProperty("ant.file", buildFile.getAbsolutePath());
		project.addBuildListener(consoleLogger);

		// Capture event for Ant script build start / stop / failure
		try {
			project.fireBuildStarted();
			project.init();
			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			project.addReference("ant.projectHelper", projectHelper);
			projectHelper.parse(project, buildFile);

			// If no target specified then default target will be executed.
			String targetToExecute = (target != null && target.trim().length() > 0) ? 
									  target.trim() :
								      project.getDefaultTarget();
									  
			project.executeTarget(targetToExecute);
			project.fireBuildFinished(null);
			
			return true;
			
		} catch (BuildException buildException) {	
			project.fireBuildFinished(buildException);
			
		}
		
		return false;
		
	}
	
	/**
	 * Logger to log output generated while executing ant script in console
	 * 
	 * @return
	 */
	private static DefaultLogger getConsoleLogger() {
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);  		//Esta linha deverá estar SEMPRE DESCOMENTADA
		//consoleLogger.setMessageOutputLevel(Project.MSG_INFO);  //Esta linha deverá estar SEMPRE DESCOMENTADA

		return consoleLogger;
	}

	/**
	 * Main method to test code
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Running default target of ant script
		boolean ok = executeAntTask("C:\\workspaces\\wrk-mestrado\\ProMergeHook\\build.xml");

		System.out.println("-----------------------------" + ok);

		// Running specified target of ant script
		// executeAntTask("build.xml", "compile");
	}

}