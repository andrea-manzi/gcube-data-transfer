package org.gcube.datatransfer.portlets.sm.user.test;

import java.io.File;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestLocal2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("items were deleted:"+delete());
		
	}

	public static boolean delete(){
		String path = "/tmp/storagemanager-portlet/test";
		File rootFolder=new File(path);
		
		boolean exists = rootFolder.exists();
		System.out.println("exists:"+exists);
	 	if(!exists){
	 		rootFolder.mkdirs();
	 		return false;
	 	}
	 	System.out.println(rootFolder.getAbsolutePath());
	 	for (File tmp : rootFolder.listFiles()){
			tmp.delete();
		}
	 	return true;
	}
}
