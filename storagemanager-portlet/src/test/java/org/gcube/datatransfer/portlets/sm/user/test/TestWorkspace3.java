package org.gcube.datatransfer.portlets.sm.user.test;

import java.io.InputStream;

import org.gcube.datatransfer.portlets.sm.user.server.workers.WorkspaceWorker;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItem;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItemType;
import org.gcube.common.homelibrary.home.workspace.exceptions.InsufficientPrivilegesException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemAlreadyExistException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.common.homelibrary.home.workspace.folder.FolderItem;
import org.gcube.common.homelibrary.home.workspace.folder.FolderItemType;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalFile;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestWorkspace3 {

	public static void main(String[] args) {
		String user = "nikolaos.drakopoulos";
		String scope = "/gcube/devsec";

		System.out.println("TestWorkspace...");

		WorkspaceWorker worker = new WorkspaceWorker();
		Workspace w = worker.getWorkspaceWithoutASL(user,scope);
		if(w!=null)System.out.println("workspace : ok");
		else System.out.println("workspace : null");
		
		String itemId="e81fbbec-f719-4c89-93d6-6c1cf2e416ef";
		WorkspaceItem item=null;
		try {
			item = w.getItem(itemId);
		} catch (ItemNotFoundException e1) {
			e1.printStackTrace();
		}
		ExternalFile file=null;
        if (item.getType() == WorkspaceItemType.FOLDER_ITEM) {
        	//handle only externalFiles ... 
            if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_FILE) {
                file = (ExternalFile)item;
                //;
            }            
            // if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_IMAGE) {
            //  	ExternalImage file = (ExternalImage)item;
            // }
            // ....
        }
        try {
        	InputStream inputstr = file.getData();
        	if(inputstr==null)System.out.println("inputstream is null");
        	else System.out.println("inputstream is not null");
        	
        	System.out.println("mime type = "+file.getMimeType());
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}
        
	}

	public static void removeItemOrFolder(Workspace w, String id){		
		try {
			w.removeItem(id);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}
	}
	public static void removeItemOrFolder(WorkspaceFolder folder, String fileName){
		try {
			folder.find(fileName).remove();
		} catch (InternalErrorException e) {
			e.printStackTrace();
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
		}
	}
	public static WorkspaceFolder getFolder(Workspace w, String idOfRootFolder) throws InternalErrorException{
		WorkspaceFolder folder=null;
		try {
			folder = (WorkspaceFolder)w.getItem(idOfRootFolder);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		}
		return folder;
	}

	public static void createFolder(WorkspaceFolder folder,String fileName){				
		try {
			folder.createFolder(fileName,fileName);
		} catch (InternalErrorException e) {
			e.printStackTrace();
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
		} catch (ItemAlreadyExistException e) {
			e.printStackTrace();
		}
	}
	public static void createFile(WorkspaceFolder folder,String fileName,InputStream streamIn,String type){
		try {
			folder.createExternalFileItem(fileName,fileName, type, streamIn);
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
		} catch (ItemAlreadyExistException e) {
			System.out.println(fileName +" already exists ...");
			System.out.println("We replace it...");
			replaceFile(folder,fileName,streamIn,type);
			//e.printStackTrace();
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}
	}
	public static void replaceFile(WorkspaceFolder root, String fileName,InputStream streamIn,String type){
		//remove
		removeItemOrFolder(root,fileName);

		ExternalFile file=null;
		//create new one
		try {
			file=root.createExternalFileItem(fileName,fileName, type, streamIn);
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
		} catch (ItemAlreadyExistException e) {
			e.printStackTrace();
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}
		if(file!=null){
			System.out.println(fileName +" successfully replaced ...");
		}
		else System.out.println("error during the replacement ...");
	}

}
