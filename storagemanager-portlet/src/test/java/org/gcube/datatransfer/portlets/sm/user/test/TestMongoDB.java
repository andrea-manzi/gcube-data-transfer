package org.gcube.datatransfer.portlets.sm.user.test;

import java.io.InputStream;
import java.util.List;


import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.contentmanagement.blobstorage.resource.StorageObject;
import org.gcube.contentmanagement.blobstorage.service.IClient;
import org.gcube.contentmanager.storageclient.wrapper.AccessType;
import org.gcube.contentmanager.storageclient.wrapper.StorageClient;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestMongoDB {
	private static IClient client;
	private static String serviceClass="data-transfer";
	private static String serviceName="scheduler-portlet";
	private static String owner="nikolaos.drakopoulos";
	private static String accessType="private";
	
	public static void main(String[] args) {
		ScopeProvider.instance.set("/gcube/devsec");
		try {
			client=new StorageClient(serviceClass, serviceName, owner, AccessType.valueOf(accessType.toUpperCase())).getClient();
		//	client=new StorageClient(serviceClass, serviceName, owner, AccessType.valueOf(accessType.toUpperCase()),scope.toString()).getClient();

		} catch (Exception e) {		e.printStackTrace();
		}
		//getSomeFile();
		testShowDir();
	}
	
	
	public static void testShowDir(){		
		String finalResult="";
		String rootPath=".";
		
		if(rootPath.startsWith("."))rootPath=rootPath.substring(1);
		if(!rootPath.startsWith("/"))rootPath="/"+rootPath;
		if(rootPath.endsWith("."))rootPath=rootPath.substring(0, rootPath.length()-1);
		if(!rootPath.endsWith("/"))rootPath=rootPath+"/";
		
		finalResult=showDir(rootPath,finalResult);
		System.out.println("ShowDir:\n"+finalResult);
		
	}
	
	public static String showDir(String path,String finalResult){
		//	System.out.println("Dir:"+path);
		finalResult=finalResult+"Dir:"+path+"\n";
		List<StorageObject> result = client.showDir().RDir(path);
		for(StorageObject obj:result){		
			if (!obj.isDirectory()){
				
				//System.out.println("File:"+obj.getName());
				finalResult=finalResult+"File:"+path+obj.getName()+" - size="+client.getSize().RFile(path+obj.getName())+"\n";
			}
			else {				
				finalResult=showDir(path+obj.getName()+"/",finalResult);
			}
		}
		return finalResult;
	}	
	
	public static void getSomeFile(){
		String filename="smp://Wikipedia_logo_silver.png?5ezvFfBOLqaqBlwCEtAvz4ch5BUu1ag3yftpCvV+gayz9bAtSsnO1/sX6pemTKbDe0qbchLexXeWgGcJlskYE8td9QSDXSZj5VSl9kdN9SN0/LRYaWUZuP4Q1J7lEiwkNMDuLTfe4rIQELLbyLZ/GVvMc4gw9mj+9OYnOZmVdLk=";
			
		String str=filename;
		String[] parts = str.split("\\?");			
		String[] partsOfMain=parts[0].split("/");
		String rpath=parts[0].replaceFirst("smp:/", "");
		String outputFile = partsOfMain[partsOfMain.length-1];

		System.out.println(rpath);
		//InputStream input = client.get().RFileAStream(rpath);
		InputStream input = client.get().RFileAsInputStream(rpath);
		if(input==null)	System.out.println("TransferWorker - some inputstream is null");
		else System.out.println("TransferWorker - inputstream is ok");
	
	}

}
