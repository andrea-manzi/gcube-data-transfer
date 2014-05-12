package org.gcube.datatransfer.portlets.sm.user.test;

import org.gcube.datatransfer.portlets.sm.user.server.workers.ListFiles;
import org.gcube.datatransfer.portlets.sm.user.server.workers.LocalWorker;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestRemoveLocalFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String username="/nick";
		String path = "/tmp/storagemanager-portlet"+username;
		
		//LocalWorker.createFile(path+"test");
		LocalWorker.removeFilesFromAFolder(path);
	}

}
