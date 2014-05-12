package org.gcube.datatransfer.portlets.sm.user.test;

import java.io.File;
import java.io.IOException;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestLocal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String name = "testfile";
		String username = "nick";
		 String path="";
         if(username!=null)path="/tmp/storagemanager-portlet/"+username+"/";
         else path="/tmp/storagemanager-portlet/";
         
         File rootFolders=new File(path);
 		rootFolders.mkdirs();
 		
         File tmpFile = new File(path+name);
         try {
			tmpFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
         
         System.out.println(tmpFile.exists());
	}

}
