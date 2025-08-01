package com.dvl.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/*
 * This example shows how to fetch a file and its properties from the repository
 * at the latest (HEAD) revision . If the file is a text (either it has no
 * svn:mime-type property at all or if has and the property value is text/-like)
 * its contents as well as properties will be displayed in the console,
 * otherwise - only properties. 
 * As an example here's a part of one of the
 * program layouts (for the default url and file path used in the program):
 *  
 * File property: svn:entry:revision=2802
 * File property: svn:entry:checksum=435f2f0d33d12907ddb6dfd611825ec9
 * File property: svn:wc:ra_dav:version-url=/repos/svnkit/!svn/ver/2795/trunk/www/license.html
 * File property: svn:entry:last-author=alex
 * File property: svn:entry:committed-date=2006-11-13T21:34:27.908657Z
 * File property: svn:entry:committed-rev=2795
 * File contents:
 * 
 * <html>
 * <head>
 * <link rel="shortcut icon" href="img/favicon.ico"/>
 * <title>SVNKit&nbsp;::&nbsp;License</title>
 * </head>
 * <body>
 * <h1>The TMate Open Source License.</h1>
 * <pre>
 * ......................................
 * ---------------------------------------------
 * Repository latest revision: 2802
 */
public class GetFileSVN {
	/*
	 * args parameter is used to obtain a repository location URL, user's account name & password to authenticate him to the server, the file path in
	 * the rpository (the file path should be relative to the the path/to/repository part of the repository location URL).
	 */
	@SuppressWarnings("deprecation")
	public static String getFile(String filePath) throws IOException {
		/*
		 * Default values:
		 */

		filePath = filePath.replaceAll("\\\\", "/");

		// https://note-armino:8443/svn/mestrado/trunk/teste3/src/teste2/Teste.java
		String url = "http://localhost:8443/svn/mestrado/trunk";
		
		/*
		 * Initializes the library (it must be done before ever using the library itself)
		 */
		setupLibrary();

		SVNRepository repository = null;
		try {
			/*
			 * Creates an instance of SVNRepository to work with the repository. All user's requests to the repository are relative to the repository
			 * location used to create this SVNRepository. SVNURL is a wrapper for URL strings that refer to repository locations.
			 */
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		} catch (SVNException svne) {
			/*
			 * Perhaps a malformed URL is the cause of this exception
			 */
			System.err.println("error while creating an SVNRepository for the location '" + url + "': " + svne.getMessage());
		}

		File file = new File("C:\\Java\\eclipse-jee-juno-win32-x86_64\\eclipse\\configuracoes.properties");

		// Obtém strem de entrada do arquivo
		FileInputStream fileInputStream = new FileInputStream(file);

		// Carrega a stream de entrada como properties
		Properties prop = new Properties();
		prop.load(fileInputStream);

		// Atualiza as propriedades
		String _usuario = prop.getProperty("usuario");

		/*
		 * User's authentication information (name/password) is provided via an ISVNAuthenticationManager instance. SVNWCUtil creates a default
		 * authentication manager given user's name and password.
		 * 
		 * Default authentication manager first attempts to use provided user name and password and then falls back to the credentials stored in the
		 * default Subversion credentials storage that is located in Subversion configuration area. If you'd like to use provided user name and
		 * password only you may use BasicAuthenticationManager class instead of default authentication manager:
		 * 
		 * authManager = new BasicAuthenticationsManager(userName, userPassword);
		 * 
		 * You may also skip this point - anonymous access will be used.
		 */
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(_usuario, "123");
		repository.setAuthenticationManager(authManager);

		/*
		 * This Map will be used to get the file properties. Each Map key is a property name and the value associated with the key is the property
		 * value.
		 */
		SVNProperties fileProperties = new SVNProperties();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			/*
			 * Checks up if the specified path really corresponds to a file. If doesn't the program exits. SVNNodeKind is that one who says what is
			 * located at a path in a revision. -1 means the latest revision.
			 */
			SVNNodeKind nodeKind = repository.checkPath(filePath, -1);

			if (nodeKind == SVNNodeKind.NONE) {
				System.err.println("There is no entry at '" + url + "'.");
			} else if (nodeKind == SVNNodeKind.DIR) {
				System.err.println("The entry at '" + url + "' is a directory while a file was expected.");
			}
			/*
			 * Gets the contents and properties of the file located at filePath in the repository at the latest revision (which is meant by a negative
			 * revision number).
			 */
			repository.getFile(filePath, -1, fileProperties, baos);

		} catch (SVNException svne) {
			System.err.println("error while fetching the file contents and properties: " + svne.getMessage());
		}

		/*
		 * Here the SVNProperty class is used to get the value of the svn:mime-type property (if any). SVNProperty is used to facilitate the work with
		 * versioned properties.
		 */
		String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);

		/*
		 * SVNProperty.isTextMimeType(..) method checks up the value of the mime-type file property and says if the file is a text (true) or not
		 * (false).
		 */
		boolean isTextType = SVNProperty.isTextMimeType(mimeType);

		/*
		 * Displays the file contents in the console if the file is a text.
		 */
		if (isTextType) {
			System.out.println("File contents:");
			System.out.println();
			try {
				File fileResult = new File("arquivoCheckout.txt");
				FileOutputStream fos = new FileOutputStream(fileResult);
				baos.writeTo(fos);

				return FileUtils.readFileToString(fileResult);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else {
			System.out
					.println("File contents can not be displayed in the console since the mime-type property says that it's not a kind of a text file.");
		}

		return null;
	}

	private static void setupLibrary() {
		/*
		 * For using over http:// and https://
		 */
		DAVRepositoryFactory.setup();
		/*
		 * For using over svn:// and svn+xxx://
		 */
		SVNRepositoryFactoryImpl.setup();

		/*
		 * For using over file:///
		 */
		FSRepositoryFactory.setup();
	}

	public static void main(String[] args) {
		String filePath = "Sample\\src\\Teste.java";

		filePath = filePath.replaceAll("\\\\", "/");

		System.out.println(filePath);
	}
}