package org.gcube.datatransfer.portlets.sm.user.server.workers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.gcube.contentmanagement.blobstorage.service.IClient;
import org.gcube.contentmanager.storageclient.wrapper.AccessType;
import org.gcube.contentmanager.storageclient.wrapper.StorageClient;
import org.gcube.datatransfer.portlets.sm.user.server.utils.Constants;
import org.gcube.datatransfer.portlets.sm.user.server.utils.TransferUtils;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.TransferDetails;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TransferWorkerOld {
	TransferDetails transferDetailsObj;
	IClient client = null;
	String sourceType=null;
	String destinationType=null;
	HttpServletRequest localRequest=null; // in case of not serializable workspace dest
	
	public boolean process(TransferDetails transfDetailsObj,HttpServletRequest request){
		localRequest=request;
		transferDetailsObj=transfDetailsObj;		
		sourceType = transferDetailsObj.getSourceType();
		destinationType = transferDetailsObj.getDestinationType();

		checkAuthentication(); //we include the authentication in url's if exists
		printInputUrls(); //optional print
		List<String> inputUrls=transferDetailsObj.getInputUrls();
		
		//workspace destination
		if(destinationType.compareTo("Workspace")==0){
			boolean errorDuringTransferToWorkspace=false;
			
			WorkspaceWorker wsWorker = new WorkspaceWorker();
			String destinationFolderId=transferDetailsObj.getDestinationFolderId();
			String serializedWorkspaceInfo=transferDetailsObj.getSerializedWorkspaceInfo();
			boolean needTheParent = transferDetailsObj.getNeedParent();
			WorkspaceFolder destination = wsWorker.getWorkspaceFolder(serializedWorkspaceInfo, destinationFolderId, needTheParent, localRequest);
			if(destination==null){
				System.out.println("TransferWorker - destination folder in workspace=null");
				return true;
			}
		//	errorDuringTransferToWorkspace=wsWorker.uploadFiles(inputUrls, destination,null);
			
		//	return errorDuringTransferToWorkspace;
			return true;
		}
		
		//mongoDB destination
		String outPath = transferDetailsObj.getDestinationFolder();
		initializeIClient(); 
		
		long endTime,startTime;
		boolean errorFlag=false;
		
		for(String tmp:inputUrls){
			startTime=0;
			try {
			FileObject inputFile=null;			
			inputFile = TransferUtils.prepareFileObject(tmp);
						
			if(inputFile==null){
				throw new Exception("inputFile=null");
			}
			else if(inputFile.getURL()==null)throw new Exception("inputFile.getURL()=null");
			
			URLConnection connection = inputFile.getURL().openConnection();
			connection.setConnectTimeout(Constants.defaultTimeOut);
			InputStream streamIn = connection.getInputStream();

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
//					String rpath=parts[0].replaceFirst("smp:/", "");
//					System.out.println("TransferWorker - process - rpath="+rpath);
//					
//					DecryptSmpUrl.decrypt(parts[1]);
//					GCUBEScope scope = GCUBEScope.getScope(DecryptSmpUrl.scopeType);		
//					IClient clientNew = new StorageClient(DecryptSmpUrl.serviceClass, DecryptSmpUrl.serviceName, DecryptSmpUrl.owner, AccessType.valueOf(DecryptSmpUrl.accessType.toUpperCase()),scope.toString()).getClient();
//					streamIn=clientNew.get().RFileAsInputStream(rpath);
				}
				String[] partsOfMain=parts[0].split("/");
				outputFile = partsOfMain[partsOfMain.length-1];
			}
			else outputFile = inputFile.getName().getBaseName();
			
			if(outPath.endsWith("/"))outPath=outPath.substring(0, outPath.length()-1);
			String absoluteOutputFile = outPath+File.separator+outputFile;


			startTime= System.currentTimeMillis();

			if(streamIn==null)System.out.println("TransferWorker - process - Error streamIn=null");
			if(streamOut==null)System.out.println("TransferWorker - process - Error streamOut=null");
			
			IOUtils.copy(streamIn, streamOut);
			
			client.put(true).LFile(tmpFile.getAbsolutePath()).RFile(absoluteOutputFile);	
			String outURL = client.getUrl().RFile(absoluteOutputFile);
			
			endTime = System.currentTimeMillis();
			long duration = endTime-startTime;
			System.out.println("TransferWorker - process - File succesfully copied to "+ outURL+" - transfer time ="+duration);
			streamIn.close();
			streamOut.close();
			
			} catch (Exception e) {
				System.out.println("TransferWorker - process - Exception for inputfile="+tmp);
				e.printStackTrace();
				errorFlag=true;
				continue;
			}
		}
		
		return errorFlag;
	}

	public void checkAuthentication(){
		//check for authencation pass 
		if(transferDetailsObj.getPass().compareTo("")!=0){
			String workspacePass=transferDetailsObj.getPass();
			String user=transferDetailsObj.getSubmitter();
			List<String> inputURIS=transferDetailsObj.getInputUrls();

			List<String> changedInputURIS=new ArrayList<String>();
			for(String tmp:inputURIS){		
				String[] partsOfLink=tmp.split("//");
				if(partsOfLink.length<2){
					System.out.println("TransferWorker - process - endpoint does not contain '//' and it is not a proper hostname");
					return ;
				}
				//authenticatedLink = "http://username:password@hostname/";
				//CHANGED
				//String authenticatedLink="webdav://"+user+":"+workspacePass+"@"+partsOfLink[1];
				//TO
				String authenticatedLink="http://"+user+":"+workspacePass+"@"+partsOfLink[1];
				
				changedInputURIS.add(authenticatedLink);
			}
			transferDetailsObj.setInputUrls(changedInputURIS);
		}
	}
	
	public void initializeIClient(){
		try {
			client = new StorageClient(
					transferDetailsObj.getServiceClass(), 
					transferDetailsObj.getServiceName(),
					transferDetailsObj.getOwner(),
					AccessType.valueOf(transferDetailsObj.getAccessType()),
					transferDetailsObj.getScope()).getClient();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("TransferWorker - StorageClient details: \n" +
				"ServiceClass="+transferDetailsObj.getServiceClass()+"\n"+
				"ServiceName="+transferDetailsObj.getServiceName()+"\n"+
				"Owner="+transferDetailsObj.getOwner()+"\n"+
				"AccessType="+transferDetailsObj.getAccessType()+"\n"+
				"Scope="+transferDetailsObj.getScope()+"\n");
	}
	
	//optional printing of intput urls
	public void printInputUrls(){
		System.out.println("TransferWorker - printInputUrls - print input URLS before transfer:");
		for(String tmp:transferDetailsObj.getInputUrls()){				
			System.out.println("'"+tmp+"'");
		}
	}
}
