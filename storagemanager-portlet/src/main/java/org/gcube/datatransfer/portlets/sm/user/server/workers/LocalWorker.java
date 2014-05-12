package org.gcube.datatransfer.portlets.sm.user.server.workers;

import java.io.File;
import java.io.IOException;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class LocalWorker {
	 
		public static void removeFilesFromAFolder(String path){
			if(path.endsWith("/"))path=path.substring(0, path.length()-1);
			
			File rootFolder = new File(path);
			boolean exists = rootFolder.exists();
			if(!exists){
		 		rootFolder.mkdirs();
		 		return;
		 	}
		 	
			for (File tmp : rootFolder.listFiles()){
				tmp.delete();
			}
		}
}
