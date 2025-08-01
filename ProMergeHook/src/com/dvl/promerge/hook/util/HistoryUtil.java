package com.dvl.promerge.hook.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.dvl.core.vos.ArquivoComitadoVO;

/*
 * The following example program demonstrates how you can use SVNRepository to
 * obtain a history for a range of revisions including (for each revision): all
 * changed paths, log message, the author of the commit, the timestamp when the 
 * commit was made. It is similar to the "svn log" command supported by the 
 * Subversion client library.
 * 
 * As an example here's a part of one of the program layouts (for the default
 * values):
 * 
 * ---------------------------------------------
 * revision: 1240
 * author: alex
 * date: Tue Aug 02 19:52:49 NOVST 2005
 * log message: 0.9.0 is now trunk
 *
 * changed paths:
 *  A  /trunk (from /branches/0.9.0 revision 1239)
 * ---------------------------------------------
 * revision: 1263
 * author: sa
 * date: Wed Aug 03 21:19:55 NOVST 2005
 * log message: updated examples, javadoc files
 *
 * changed paths:
 *  M  /trunk/doc/javadoc-files/javadoc.css
 *  M  /trunk/doc/javadoc-files/overview.html
 *  M  /trunk/doc/examples/src/org/tmatesoft/svn/examples/wc/StatusHandler.java
 * ...
 * 
 */
public class HistoryUtil {
	/*
	 * args parameter is used to obtain a repository location URL, a start revision number, an end revision number, user's account name &
	 * password to authenticate him to the server.
	 */
	@SuppressWarnings("deprecation")
	public static List<ArquivoComitadoVO> getFilesFromRevision(long startRevision, long endRevision) throws SVNException, IOException {
		/*
		 * Default values:
		 */
		String url = "http://localhost:8443/svn/mestrado/";

		/*
		 * Initializes the library (it must be done before ever using the library itself)
		 */
		setupLibrary();

		SVNRepository repository = null;

		try {
			/*
			 * Creates an instance of SVNRepository to work with the repository. All user's requests to the repository are relative to the
			 * repository location used to create this SVNRepository. SVNURL is a wrapper for URL strings that refer to repository
			 * locations.
			 */
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));

		} catch (SVNException svne) {
			/*
			 * Perhaps a malformed URL is the cause of this exception.
			 */
			System.err.println("error while creating an SVNRepository for the location '" + url + "': " + svne.getMessage());
			System.exit(1);
		}
		/*

		File file = new File("C:\\Java\\eclipse-jee-juno-win32-x86_64\\eclipse\\configuracoes.properties");

		// Obtém strem de entrada do arquivo
		FileInputStream fileInputStream = new FileInputStream(file);

		// Carrega a stream de entrada como properties
		Properties prop = new Properties();
		prop.load(fileInputStream);

		// Atualiza as propriedades
		String name = prop.getProperty("usuario");*/

		/*
		 * User's authentication information (name/password) is provided via an ISVNAuthenticationManager instance. SVNWCUtil creates a
		 * default authentication manager given user's name and password.
		 * 
		 * Default authentication manager first attempts to use provided user name and password and then falls back to the credentials
		 * stored in the default Subversion credentials storage that is located in Subversion configuration area. If you'd like to use
		 * provided user name and password only you may use BasicAuthenticationManager class instead of default authentication manager:
		 * 
		 * authManager = new BasicAuthenticationsManager(userName, userPassword);
		 * 
		 * You may also skip this point - anonymous access will be used.
		 */
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager("Carlos.Carbonera", "123");
		repository.setAuthenticationManager(authManager);

		// /*
		// * Gets the latest revision number of the repository
		// */
		// try {
		// endRevision = repository.getLatestRevision();
		// } catch (SVNException svne) {
		// System.err
		// .println("error while fetching the latest repository revision: "
		// + svne.getMessage());
		// System.exit(1);
		// }

		@SuppressWarnings("rawtypes")
		Collection logEntries = null;
		try {
			/*
			 * Collects SVNLogEntry objects for all revisions in the range defined by its start and end points [startRevision, endRevision].
			 * For each revision commit information is represented by SVNLogEntry.
			 * 
			 * the 1st parameter (targetPaths - an array of path strings) is set when restricting the [startRevision, endRevision] range to
			 * only those revisions when the paths in targetPaths were changed.
			 * 
			 * the 2nd parameter if non-null - is a user's Collection that will be filled up with found SVNLogEntry objects; it's just
			 * another way to reach the scope.
			 * 
			 * startRevision, endRevision - to define a range of revisions you are interested in; by default in this program -
			 * startRevision=0, endRevision= the latest (HEAD) revision of the repository.
			 * 
			 * the 5th parameter - a boolean flag changedPath - if true then for each revision a corresponding SVNLogEntry will contain a
			 * map of all paths which were changed in that revision.
			 * 
			 * the 6th parameter - a boolean flag strictNode - if false and a changed path is a copy (branch) of an existing one in the
			 * repository then the history for its origin will be traversed; it means the history of changes of the target URL (and all that
			 * there's in that URL) will include the history of the origin path(s). Otherwise if strictNode is true then the origin path
			 * history won't be included.
			 * 
			 * The return value is a Collection filled up with SVNLogEntry Objects.
			 */
			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);

		} catch (SVNException svne) {
			System.out.println("error while collecting log information for '" + url + "': " + svne.getMessage());
			System.exit(1);
		}

		List<ArquivoComitadoVO> listaRetorno = new ArrayList<ArquivoComitadoVO>(0);

		for (@SuppressWarnings("rawtypes")
		Iterator entries = logEntries.iterator(); entries.hasNext();) {
			/*
			 * gets a next SVNLogEntry
			 */
			SVNLogEntry logEntry = (SVNLogEntry) entries.next();
			// System.out.println("---------------------------------------------");
			/*
			 * gets the revision number
			 */
			// System.out.println("revision: " + logEntry.getRevision());
			/*
			 * gets the author of the changes made in that revision
			 */
			// System.out.println("author: " + logEntry.getAuthor());
			/*
			 * gets the time moment when the changes were committed
			 */
			// System.out.println("date: " + logEntry.getDate());
			/*
			 * gets the commit log message
			 */
			// System.out.println("log message: " + logEntry.getMessage());
			/*
			 * displaying all paths that were changed in that revision; changed path information is represented by SVNLogEntryPath.
			 */
			if (logEntry.getChangedPaths().size() > 0) {
				// System.out.println();
				// System.out.println("changed paths:");
				/*
				 * keys are changed paths
				 */
				@SuppressWarnings("rawtypes")
				Set changedPathsSet = logEntry.getChangedPaths().keySet();

				for (@SuppressWarnings("rawtypes")
				Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
					/*
					 * obtains a next SVNLogEntryPath
					 */
					SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPaths.next());

					SVNNodeKind nodeKind = repository.checkPath(entryPath.getPath(), entryPath.getCopyRevision());

					if (nodeKind == SVNNodeKind.NONE) {
						System.err.println("There is no entry at '" + url + "'.");
						continue;
					} else if (nodeKind == SVNNodeKind.DIR) {
						System.err.println("The entry at '" + url + "' is a directory while a file was expected.");
						continue;
					}

					SVNProperties fileProperties = new SVNProperties();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					repository.getFile(entryPath.getPath(), entryPath.getCopyRevision(), fileProperties, baos);

					Map<String, SVNPropertyValue> mapProperties = fileProperties.asMap();
					for (Entry<String, SVNPropertyValue> object : mapProperties.entrySet()) {
						System.out.println("chave:" + object.getKey());
						System.out.println("value:" + object.getValue());
					}

					String mimeType = (String) fileProperties.getStringValue(SVNProperty.MIME_TYPE);
					boolean isTextType = SVNProperty.isTextMimeType(mimeType);

					if (!isTextType) {
						continue;
					}

					System.out.println("File contents:");
					System.out.println();

					try {

						String out = new String(baos.toByteArray(), "UTF-8");

						ArquivoComitadoVO vo = new ArquivoComitadoVO();
						vo.setConteudoArquivo(out);
						vo.setNomeCompletoArquivo(entryPath.getPath());
						vo.setNomeUsuario(logEntry.getAuthor());
						vo.setRevisao(String.valueOf(startRevision));

						listaRetorno.add(vo);

					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}

		return listaRetorno;
	}

	/*
	 * Initializes the library to work with a repository via different protocols.
	 */
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

	public static void main(String[] args) throws SVNException, IOException {

		List<ArquivoComitadoVO> filesFromRevision = getFilesFromRevision(109, 109);

		for (ArquivoComitadoVO arquivoComitadoVO : filesFromRevision) {
			System.out.println("arquivo:" + arquivoComitadoVO.getNomeCompletoArquivo());
			System.out.println("conteúdo:" + arquivoComitadoVO.getConteudoArquivo());
			System.out.println("usuário:" + arquivoComitadoVO.getNomeUsuario());
			System.out.println("revisão:" + arquivoComitadoVO.getRevisao());
		}

	}
}