/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package com.dvl.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * This examples demonstrate how you can run WORKING:HEAD diff.
 * 
 * @version 1.2.0
 * @author TMate Software Ltd.
 */
public class DiffWCToRepository {

	/**
	 * Pass the absolute path of the base directory where all example data will be created in arg[0]. The sample will create:
	 * 
	 * - arg[0]/exampleRepository - repository with some test data - arg[0]/exampleWC - working copy checked out from exampleRepository
	 * 
	 * @param string
	 */
	public static void doDiff(String arquivoLocalAlterado, String arquivoServidor, String usuario) {

		// initialize SVNKit to work through file:/// protocol
		SVNUtility.initializeFSFSprotocol();

		File baseDirectory = new File("./");
		File reposRoot = new File(baseDirectory, "exampleRepository-" + usuario);
		File wcRoot = new File(baseDirectory, "exampleWC-" + usuario);

		try {

			FileUtils.deleteDirectory(reposRoot);
			FileUtils.deleteDirectory(wcRoot);

			// first create a repository and fill it with data
			SVNUtility.createRepository(reposRoot);
			SVNCommitInfo info = SVNUtility.criarEstruturaRepositorioPadrao(reposRoot, "teste.txt");
			System.out.println(info);

			// checkout the entire repository tree
			SVNURL reposURL = SVNURL.fromFile(reposRoot);

			SVNRepository repoCopy = SVNRepositoryFactory.create(reposURL);

			SVNUtility.checkOutWorkingCopy(reposURL, wcRoot);

			// now make some changes to the working copy
			SVNUtility.writeToFile(new File(wcRoot, "A/teste.txt"), arquivoServidor, false);

			SVNClientManager clientManager = SVNClientManager.newInstance();

			System.out.println(repoCopy.getLatestRevision());

			SVNCommitClient commitClient = clientManager.getCommitClient();
			commitClient.doCommit(new File[] { wcRoot }, false, "committing changes", null, null, false, false, SVNDepth.INFINITY);

			long latestRevision = repoCopy.getLatestRevision();

			System.out.println(latestRevision);

			SVNUtility.writeToFile(new File(wcRoot, "A/teste.txt"), arquivoLocalAlterado, false);

			// now run diff the working copy against the repository
			SVNDiffClient diffClient = clientManager.getDiffClient();

			final List<String> arquivosModificados = new ArrayList<String>(0);

			diffClient.doDiffStatus(reposURL, SVNRevision.create(1), reposURL, SVNRevision.create(2), SVNDepth.INFINITY, true,
					new ISVNDiffStatusHandler() {

						@Override
						public void handleDiffStatus(SVNDiffStatus diffStatus) throws SVNException {

							System.out.println("\n\nDiff Status > " + diffStatus);
							System.out.println("Path > " + diffStatus.getPath());
							System.out.println("File > " + diffStatus.getFile());
							System.out.println("Kind > " + diffStatus.getKind());
							System.out.println("Modification Type > " + diffStatus.getModificationType());
							System.out.println("URL > " + diffStatus.getURL());
							System.out.println("Properties > " + diffStatus.isPropertiesModified());

							SVNStatusType status = diffStatus.getModificationType();
							String path = diffStatus.getPath();
							if (status == SVNStatusType.STATUS_NORMAL) {
								System.out.println("N  : " + path);
							} else if (status == SVNStatusType.STATUS_NONE) {
								System.out.println("NC  : " + path);
							} else if (status == SVNStatusType.STATUS_ADDED) {
								System.out.println("A  : " + path);
							} else if (status == SVNStatusType.STATUS_DELETED) {
								System.out.println("D  : " + path);
							} else if (status == SVNStatusType.STATUS_MODIFIED) {
								arquivosModificados.add(path);
							}
						}
					});

			if (arquivosModificados == null || arquivosModificados.isEmpty())
				return;

			System.out.println("Executando metodo doDiff...");

			File fileResult = new File("result.txt");
			FileOutputStream fos = new FileOutputStream(fileResult);

			/*
			 * This corresponds to 'svn diff -rBASE:HEAD'.
			 */
			diffClient.doDiff(wcRoot, SVNRevision.UNDEFINED, SVNRevision.create(1), SVNRevision.create(2), SVNDepth.INFINITY, true, fos, null);

			fos.close();

		} catch (SVNException svne) {
			System.out.println(svne.getErrorMessage());
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(0);
		}
	}
}