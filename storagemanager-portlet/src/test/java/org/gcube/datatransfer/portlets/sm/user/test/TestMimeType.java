package org.gcube.datatransfer.portlets.sm.user.test;

import java.net.URLConnection;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestMimeType {

	public static void main(String[] args) {
		//String rpath="smp://testing/nick/tes.t.png";
		String rpath="test";

		String mimeType= URLConnection.guessContentTypeFromName(rpath);
		
		System.out.println("mimeType="+mimeType);
	}

}
