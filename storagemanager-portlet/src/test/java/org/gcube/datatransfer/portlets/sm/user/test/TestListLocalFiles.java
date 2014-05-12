package org.gcube.datatransfer.portlets.sm.user.test;

import org.gcube.datatransfer.portlets.sm.user.server.workers.ListFiles;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestListLocalFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String username="/nick";
		String path = "/tmp/storagemanager-portlet"+username;
		
		ListFiles listFiles = new ListFiles(path);
		FolderDto folder = listFiles.process();
		listFiles.printFolder(folder, 0);
	}

}
