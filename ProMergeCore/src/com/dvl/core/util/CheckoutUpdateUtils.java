package com.dvl.core.util;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class CheckoutUpdateUtils {
	public static long doCheckoutUpdate(String url, File dirProjeto, boolean checkout, String usuario) {

		long latestRevision = 0;

		try {
			// initiate the reporitory from the url
			SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(url));

			// create authentication data
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(usuario, "123");
			repository.setAuthenticationManager(authManager);

			// output some data to verify connection
			System.out.println("Repository Root: " + repository.getRepositoryRoot(true));
			System.out.println("Repository UUID: " + repository.getRepositoryUUID(true));

			// need to identify latest revision
			latestRevision = repository.getLatestRevision();
			System.out.println("Repository Latest Revision: " + latestRevision);

			// create client manager and set authentication
			SVNClientManager ourClientManager = SVNClientManager.newInstance();
			ourClientManager.setAuthenticationManager(authManager);

			// use SVNUpdateClient to do the export
			SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
			updateClient.setIgnoreExternals(false);

			// Faz Checkout
			if (checkout) {
				updateClient.doCheckout(repository.getLocation(), dirProjeto, SVNRevision.create(latestRevision),
						SVNRevision.create(latestRevision), true);

			} else {
				// Faz Update
				updateClient.doUpdate(dirProjeto, SVNRevision.create(latestRevision), true);

			}

		} catch (SVNException e) {
			e.printStackTrace();
			
		} finally {
			System.out.println("Done");
			
		}
		
		return latestRevision;
		
	}
}