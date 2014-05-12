package org.gcube.datatransfer.portlets.sm.user.test;

import org.gcube.datatransfer.portlets.sm.user.server.utils.TransferUtils;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestEncodeDecode {


	public static void main(String[] args) {
		//String tmp="Fira_at_Santorini_%28from_north%29.jpg";
		String tmp="Screenshot fr?L>>dasom 2012-12-1 09:44:16.png";
		tmp = tmp.replaceAll(" ", "_");
		
		/*tmp=TransferUtils.encodeSomeName(tmp);
		System.out.println("encoded filename="+tmp);*/
		
		tmp=TransferUtils.decodeSomeNameCompletelly(tmp);
		System.out.println("decoded filename="+tmp);
	}

}
