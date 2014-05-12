package org.gcube.datatransfer.portlets.sm.user.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.gcube.datatransfer.portlets.sm.user.server.utils.Constants;
import org.gcube.datatransfer.portlets.sm.user.server.utils.TransferUtils;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestVfs {

	public static void main(String[] args) {
		String tmp="file:///tmp/storagemanager-portlet/testing/Fira_at_Santorini_(from_north).jpg";
		
		try{
		FileObject inputFile=null;			
		inputFile = TransferUtils.prepareFileObject(tmp);
		File temp = new File(tmp);
		
		System.out.println("inputFile.getURL()="+inputFile.getURL());
		System.out.println("tmp="+tmp);
		
		URLConnection connection = inputFile.getURL().openConnection();
		connection.setConnectTimeout(Constants.defaultTimeOut);
		InputStream streamIn=null;
		try{
			streamIn = connection.getInputStream();
		}
		catch(Exception e2){
			System.out.println("when get the inputstream");
			throw e2;
		}
		File tmpFile=null;
		tmpFile = File.createTempFile("storagemanager-portlet", ".tmp", new File("/tmp"));
		
		OutputStream streamOut = null;
		streamOut = new FileOutputStream(tmpFile);

		//getting outfile info	
		String outputFile;
		if(inputFile.getURL().toString().startsWith("smp")){			
			String str=tmp;
			String[] parts = str.split("\\?");
			if(streamIn==null){
//				String rpath=parts[0].replaceFirst("smp:/", "");
//				System.out.println("TransferWorker - process - rpath="+rpath);
//				
//				DecryptSmpUrl.decrypt(parts[1]);
//				GCUBEScope scope = GCUBEScope.getScope(DecryptSmpUrl.scopeType);		
//				IClient clientNew = new StorageClient(DecryptSmpUrl.serviceClass, DecryptSmpUrl.serviceName, DecryptSmpUrl.owner, AccessType.valueOf(DecryptSmpUrl.accessType.toUpperCase()),scope.toString()).getClient();
//				streamIn=clientNew.get().RFileAsInputStream(rpath);
			}
			String[] partsOfMain=parts[0].split("/");
			outputFile = partsOfMain[partsOfMain.length-1];
		}
		else outputFile = inputFile.getName().getBaseName();
		
		//if(outPath.endsWith("/"))outPath=outPath.substring(0, outPath.length()-1);
		//String absoluteOutputFile = outPath+File.separator+outputFile;


		long startTime= System.currentTimeMillis();

		if(streamIn==null)System.out.println("TransferWorker - process - Error streamIn=null");
		if(streamOut==null)System.out.println("TransferWorker - process - Error streamOut=null");
		
		IOUtils.copy(streamIn, streamOut);
		
		long endTime= System.currentTimeMillis();
		long duration = endTime-startTime;
		System.out.println("Test - been transferred... duration="+duration);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
