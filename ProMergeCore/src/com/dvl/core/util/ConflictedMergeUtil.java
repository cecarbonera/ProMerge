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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.ISVNConflictHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNConflictDescription;
import org.tmatesoft.svn.core.wc.SVNConflictReason;
import org.tmatesoft.svn.core.wc.SVNConflictResult;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNMergeFileSet;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;

import com.dvl.core.vos.IntegracaoVO;

/**
 * @version 1.2.0
 * @author TMate Software Ltd.
 */
public class ConflictedMergeUtil {

	private static Logger log = Logger.getLogger(ConflictedMergeUtil.class);

	private static SVNConflictChoice choice = SVNConflictChoice.THEIRS_CONFLICT;

	/**
	 * Pass the absolute path of the base directory where all example data will be created in arg[0]. The sample will create:
	 * 
	 * - arg[0]/exampleRepository - repository with some test data - arg[0]/exampleWC - working copy checked out from exampleRepository
	 */
	public static Map<String, String> integrarArquivos(File diretorioBase, String nomeUsuario, String conteudoArquivo, String nomeCompletoArquivo,
			String arquivoOutroWorkspace, boolean merge) {

		if (merge) {
			choice = SVNConflictChoice.MERGED;
		}

		// initialize SVNKit to work through file:/// protocol
		SVNUtility.initializeFSFSprotocol();

		File reposRoot = new File(diretorioBase, "repos-aux-" + nomeUsuario);
		File wcRoot = new File(diretorioBase, "wc-" + nomeUsuario);

		try {

			FileUtils.deleteDirectory(reposRoot);

			FileUtils.deleteDirectory(wcRoot);

			// first create a repository and fill it with data
			SVNUtility.createRepository(reposRoot);

			SVNCommitInfo info = SVNUtility.criarEstruturaRepositorioPadrao(reposRoot, "teste.txt");

			// print out new revision info
			log.info(info);
			System.out.println(info);

			SVNClientManager clientManager = SVNClientManager.newInstance();

			SVNURL reposURL = SVNURL.fromFile(reposRoot);

			// copy A to A_copy in repository (url-to-url copy)
			SVNCopyClient copyClient = clientManager.getCopyClient();
			SVNURL A_URL = reposURL.appendPath("A", true);
			SVNURL copyTargetURL = reposURL.appendPath("B", true);
			SVNCopySource copySource = new SVNCopySource(SVNRevision.UNDEFINED, SVNRevision.HEAD, A_URL);
			info = copyClient.doCopy(new SVNCopySource[] { copySource }, copyTargetURL, false, false, true, "copy A to B", null);

			// print out new revision info
			System.out.println(info);
			log.info(info);

			// checkout the entire repository tree
			SVNUtility.checkOutWorkingCopy(reposURL, wcRoot);

			// now make some changes to the A tree
			SVNUtility.writeToFile(new File(wcRoot, "A/" + "teste.txt"), conteudoArquivo, true);

			// commit local changes
			SVNCommitClient commitClient = clientManager.getCommitClient();
			SVNCommitInfo doCommit = commitClient.doCommit(new File[] { wcRoot }, false, "committing changes", null, null, false, false,
					SVNDepth.INFINITY);

			System.out.println(doCommit);

			// now make some local changes to the A_copy tree
			// change file contents of A_copy/B/lambda and A_copy/mu
			SVNUtility.writeToFile(new File(wcRoot, "B/" + "teste.txt"), arquivoOutroWorkspace, true);

			// now diff the base revision of the working copy against the
			// repository
			SVNDiffClient diffClient = clientManager.getDiffClient();

			/*
			 * Since we provided no custom ISVNOptions implementation to SVNClientManager, our manager uses DefaultSVNOptions, which is set to all
			 * SVN*Client classes which the manager produces. So, we can cast ISVNOptions to DefaultSVNOptions.
			 */
			DefaultSVNOptions options = (DefaultSVNOptions) diffClient.getOptions();
			// This way we set a conflict handler which will automatically
			// resolve conflicts for those
			// cases that we would like
			options.setConflictHandler(new ConflictResolverHandler());

			/*
			 * do the same merge call, merge-tracking feature will merge only those revisions which were not still merged.
			 */
			SVNRevisionRange rangeToMerge = new SVNRevisionRange(SVNRevision.create(1), SVNRevision.HEAD);

			diffClient.doMerge(A_URL, SVNRevision.HEAD, Collections.singleton(rangeToMerge), new File(wcRoot, "B"), SVNDepth.UNKNOWN, true, false,
					false, false);

			String arquivoIntegrado = FileUtils.readFileToString(new File(wcRoot, "B/" + "teste.txt"));

			Map<String, String> retorno = new HashMap<String, String>();
			retorno.put("arquivo", arquivoIntegrado);
			retorno.put("conflito", "0");
			retorno.put("conflito", "0");

			//
			if (arquivoIntegrado.contains("<<<<<<<") || arquivoIntegrado.contains("=======") || arquivoIntegrado.contains(">>>>>>>")) {
				retorno.put("conflito", "1");
			}

			return retorno;

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Erro ao integrar arquivos.", e);
		}

		return null;
	}

	/**
	 * Conflict resolver which always selects the local version of a file.
	 * 
	 * @version 1.2.0
	 * @author TMate Software Ltd.
	 */
	private static class ConflictResolverHandler implements ISVNConflictHandler {

		public SVNConflictResult handleConflict(SVNConflictDescription conflictDescription) throws SVNException {

			SVNConflictReason reason = conflictDescription.getConflictReason();
			SVNMergeFileSet mergeFiles = conflictDescription.getMergeFiles();

			if (reason == SVNConflictReason.EDITED) {
				// If the reason why conflict occurred is local edits, chose
				// local version of the file
				// Otherwise the repository version of the file will be chosen.
				// choice = SVNConflictChoice.MINE_CONFLICT;
			}
			System.out.println("Automatically resolving conflict for " + mergeFiles.getWCFile() + ", choosing "
					+ (choice == SVNConflictChoice.MINE_FULL ? "local file" : "repository file"));
			return new SVNConflictResult(choice, mergeFiles.getResultFile());
		}

	}

	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}

	public static void main(String[] args) throws IOException {

		File file = new File("C:\\Java\\eclipse-jee-juno-win32-x86_64\\eclipse\\configuracoes.properties");
		log.info(file.getAbsolutePath());

		// Obtém strem de entrada do arquivo
		FileInputStream fileInputStream = new FileInputStream(file);

		// Carrega a stream de entrada como properties
		Properties prop = new Properties();
		prop.load(fileInputStream);
		String usuario = prop.getProperty("usuario");
		
		File dirBase = new File("./");
		IntegracaoVO vo = new IntegracaoVO();
		vo.setConteudoArquivo("Arquivo wrk atual");
		vo.setNomeCompletoArquivo("teste.txt");
		vo.setNomeUsuarioRequisicao(usuario);

		integrarArquivos(dirBase, vo.getNomeUsuarioRequisicao(), vo.getConteudoArquivo(), vo.getNomeCompletoArquivo(), "Arquivo wrk atual", false);
	}

}