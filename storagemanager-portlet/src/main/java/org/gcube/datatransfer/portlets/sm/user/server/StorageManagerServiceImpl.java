package org.gcube.datatransfer.portlets.sm.user.server;

import java.util.Map;

import javax.servlet.ServletException;

import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.datatransfer.portlets.sm.user.server.workers.ConnectionSMP;
import org.gcube.datatransfer.portlets.sm.user.server.workers.ListFiles;
import org.gcube.datatransfer.portlets.sm.user.server.workers.LocalWorker;
import org.gcube.datatransfer.portlets.sm.user.server.workers.TransferWorker;
import org.gcube.datatransfer.portlets.sm.user.server.workers.WorkspaceWorker;
import org.gcube.datatransfer.portlets.sm.user.shared.StorageManagerService;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.TransferDetails;

import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;


@SuppressWarnings("serial")
public class StorageManagerServiceImpl extends RemoteServiceServlet implements
StorageManagerService {

	@Override
	public void init() throws ServletException {
		super.init();
		System.out.println("WorkspaceAreaServiceImpl Init...");
	}

	public Map<String, String> transfer(String serializedTransferDetails) throws IllegalArgumentException {
		TransferDetails transferDetailsObj;
		Gson gson = new Gson();
		transferDetailsObj= gson.fromJson(serializedTransferDetails, TransferDetails.class);
		
		TransferWorker transferWorker = new TransferWorker();
		Map<String, String>  handles = transferWorker.process(transferDetailsObj,this.getThreadLocalRequest());
		// either link or workspace id
		return handles;
	}
	
	public String listFiles(String input) throws IllegalArgumentException {
		String rootPath = input;
		ListFiles listFiles = new ListFiles(rootPath);
		FolderDto folder = listFiles.process();
		
		if(folder!=null){
			System.out.println("GET LIST OF LOCALFILES: returned folder==null");			
		}
		Gson gson = new Gson();
		String jsonString = gson.toJson(folder);
		System.out.println("GET LIST OF LOCALFILES: gson folder length= "+jsonString.length());
		return jsonString;
	}

	public String getFileListOfMongoDB(String smServiceClassSource,String smServiceNameSource,String smOwnerSource,String smAccessTypeSource, String smAreaTypeSource, String path,String scope){
		FolderDto rootFolder=null;

		ConnectionSMP connectionSMP = null;
		connectionSMP = ConnectionSMP.getInstance(
				smServiceClassSource,
				smServiceNameSource,
				smOwnerSource,
				smAccessTypeSource,
				smAreaTypeSource,
				scope,path
			);

		rootFolder = connectionSMP.browse(path);

		if(rootFolder==null){
			System.out.println("GET LIST OF MONGODBSOURCE: returned folder==null");			
			return null;
		}
		Gson gson = new Gson();
		String jsonString = gson.toJson(rootFolder);
		System.out.println("GET LIST OF MONGODBSOURCE: gson folder length= "+jsonString.length());

		return jsonString;	
	}

	public void createNewFolderInMongoDB(String smServiceClassSource,String smServiceNameSource,String smOwnerSource,String smAccessTypeSource, String smAreaTypeSource, String path,String scope){
		ConnectionSMP connectionSMP = null;
		connectionSMP = ConnectionSMP.getInstance(smServiceClassSource,smServiceNameSource,smOwnerSource,smAccessTypeSource, smAreaTypeSource, scope,"/");
		connectionSMP.storeNewFolder(path);
	}
	public void deleteFolderInMongoDB(String smServiceClassSource,String smServiceNameSource,String smOwnerSource,String smAccessTypeSource, String smAreaTypeSource, String path,String scope){
		ConnectionSMP connectionSMP = null;
		connectionSMP = ConnectionSMP.getInstance(smServiceClassSource,smServiceNameSource,smOwnerSource,smAccessTypeSource,smAreaTypeSource, scope,"/");
		connectionSMP.deleteFolder(path);
	}
	
	public void deleteUploadedFiles(String path){
		LocalWorker.removeFilesFromAFolder(path);
	}
	
	/*
	 * getUserAndScope
	 * input: Nothing
	 * returns: String with the user name and the scope 
	 */
	public String getUserAndScopeAndRole(){
		WorkspaceWorker workspWorker = new WorkspaceWorker();	
		String returnedValue=workspWorker.getUserAndScopeAndRole(this.getThreadLocalRequest());

		//returning value .. example: nick--/gcube/devsec
		if(returnedValue==null)System.out.println("GET USER AND SCOPE AND ROLE: returnedValue==null");
		return returnedValue;
	}

	/*
	 * getWorkspace
	 * input: String with the user 
	 * returns: Json String with the Workspace object 
	 */
	public String getWorkspace(String username){
		if(username==null){	System.out.println("GET WORKSPACE: username==null");return null;}
		WorkspaceWorker workspWorker = new WorkspaceWorker();
		
		//testingDataArea
		//System.out.println("GET WORKSPACE: calling testingDataArea ... *****");
		//workspWorker.testingDataArea(this.getThreadLocalRequest());
		
		String serializedObject=workspWorker.getWorkspace(this.getThreadLocalRequest());

		if(serializedObject!=null)return serializedObject;
		else {
			System.out.println("GET WORKSPACE: serializedObject==null");
			return null;
		}
	}

	/*
	 * getWorkspaceFolder
	 * input: Json String with the Workspace obj
	 * input: String with the id of the folder if exist, in other case we take the root folder
	 * returns: Json String with the tree starting from the folder
	 */
	public String getWorkspaceFolder(String serializedWorkspaceInfo, String folderId, boolean needTheParent) throws IllegalArgumentException {
		Workspace workspace=null;
		WorkspaceWorker workerWS=new WorkspaceWorker();
		
		//testingDataArea
		//System.out.println("getWorkspaceFolder: calling testingDataArea ... *****");
		//workerWS.testingDataArea(this.getThreadLocalRequest());
					
		//CHANGED - we do not use serialized workspace anymore 
		workspace = workerWS.getWorkspaceWithoutSerialization(this.getThreadLocalRequest());
		
		
		if(workspace==null){System.out.println("GET WORKSPACE FOLDER: workspace= null");return null;}

		String workspaceWebDavLink=null;
		try {
			workspaceWebDavLink=workspace.getUrlWebDav();
		} catch (InternalErrorException e1) {
			e1.printStackTrace();
		}
		if(workspaceWebDavLink==null){System.out.println("GET WORKSPACE FOLDER: workspaceWebDavLink= null");return null;}


		WorkspaceFolder root=null;
		//String rootParent=null;
		if(folderId==null){
			root = workspace.getRoot();
		}
		else {
			try {
				root = (WorkspaceFolder) workspace.getItem(folderId);
				if(needTheParent)root=root.getParent(); // take the parent instead

			} catch (ItemNotFoundException e) {
				e.printStackTrace();
			}catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}

		if(root==null){System.out.println("GET WORKSPACE FOLDER: root= null");return null;}

		WorkspaceWorker wsWorker = new WorkspaceWorker();
		FolderDto folder = null;
		try {
			folder = wsWorker.createTree(root, workspaceWebDavLink);
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}		
		if(folder==null){System.out.println("GET WORKSPACE FOLDER: folder= null");return null;}
		else {
			//folder.setParentIdInWorkspace(rootParent);
			wsWorker.printFolder(folder,0);
			Gson gson2 = new Gson();
			String jsonString = gson2.toJson(folder);
			System.out.println("GET WORKSPACE FOLDER: gson folder length= "+jsonString.length());

			return jsonString;
		}
	}
	
	public String createNewFolderInWorkspace(String serializedWorkspaceInfo, String parentFolderId,String newFolderName){
		WorkspaceWorker wsWorker = new WorkspaceWorker();
		WorkspaceFolder parentFolder = wsWorker.getWorkspaceFolder(serializedWorkspaceInfo, parentFolderId, false, getThreadLocalRequest());
		if(parentFolder==null){System.out.println("CREATE NewFolderInWorkspace: parentFolder==null");return null;}
		
		return wsWorker.createFolder(parentFolder, newFolderName);
	}
	
	public boolean deleteInWorkspace(String serializedWorkspaceInfo, String itemIdToRemove){
		WorkspaceWorker wsWorker = new WorkspaceWorker();
		boolean error = false;
		error = wsWorker.removeItemOrFolder(serializedWorkspaceInfo, itemIdToRemove, getThreadLocalRequest());
		return error;
	}
}
