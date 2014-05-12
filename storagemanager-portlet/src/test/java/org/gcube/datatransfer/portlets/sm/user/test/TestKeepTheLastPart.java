package org.gcube.datatransfer.portlets.sm.user.test;

import org.gcube.datatransfer.portlets.sm.user.client.utils.Utils;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestKeepTheLastPart {

	public static void main(String[] args) {
		String testWindowsPath = "C:\\facepath\\cartoon.jpg"; 
		//String testWindowsPath = "C:/facepath//cartoon.jpg//"; 
		System.out.println(Utils.keepOnlyTheLastPart(testWindowsPath));
	}

}
