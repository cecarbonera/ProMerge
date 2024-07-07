/*
 * ====================================================================
 * Copyright (c) 2004-2011 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

package com.dvl.core.util; 

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

/**
 * @version 1.3
 * @author TMate Software Ltd. 
 */
public class SVNUtility {
 
	public static void createRepository(File reposRoot) throws SVNException {
		SVNAdminClient adminClient = SVNClientManager.newInstance().getAdminClient();
		adminClient.doCreateRepository(reposRoot, null, true, false, false, false);
	}

	public static void checkOutWorkingCopy(SVNURL url, File wcRoot) throws SVNException {
		SVNUpdateClient updateClient = SVNClientManager.newInstance().getUpdateClient();
		updateClient.doCheckout(url, wcRoot, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, false);
	}

	public static void writeToFile(File file, String text, boolean append) throws IOException {
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		OutputStream outputStream = null;
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(file, append));
			outputStream.write(text.getBytes());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	public static void initializeFSFSprotocol() {
		/*
		 * Call this setup method only once in you program and prior to using SVNKit.
		 */
		FSRepositoryFactory.setup();
	}

	/**
	 * Cria estrutura padrão
	 * 
	 * @param reposRoot
	 * @param nomeArquivo
	 * @return
	 * @throws SVNException
	 */
	public static SVNCommitInfo criarEstruturaRepositorioPadrao(File reposRoot, String nomeArquivo) throws SVNException {

		SVNURL reposURL = SVNURL.fromFile(reposRoot);
		SVNRepository repos = SVNRepositoryFactory.create(reposURL);

		ISVNEditor commitEditor = repos.getCommitEditor("initializing the repository", null, false, null, null);

		SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();

		commitEditor.openRoot(SVNRepository.INVALID_REVISION);

		// add /A directory
		commitEditor.addDir("A", null, SVNRepository.INVALID_REVISION);

		// add /A/mu file
		String string = "A/" + nomeArquivo;
		commitEditor.addFile(string, null, SVNRepository.INVALID_REVISION);
		commitEditor.applyTextDelta(string, null);
		String checksum = deltaGenerator.sendDelta(string, new ByteArrayInputStream("".getBytes()), commitEditor, true);
		commitEditor.closeFile(string, checksum);

		// close /A
		commitEditor.closeDir();

		// close the root of the edit - / (repository root) in our case
		commitEditor.closeDir();

		return commitEditor.closeEdit();
	}
}