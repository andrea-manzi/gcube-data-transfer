package org.gcube.datatransfer.portlets.sm.user.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.gcube.datatransfer.portlets.sm.user.client.utils.Utils;
import org.gcube.datatransfer.portlets.sm.user.server.utils.Constants;
import org.gcube.datatransfer.portlets.sm.user.server.utils.TransferUtils;
import org.gcube.datatransfer.portlets.sm.user.server.workers.WorkspaceWorker;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItem;
import org.gcube.common.homelibrary.home.workspace.exceptions.InsufficientPrivilegesException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemAlreadyExistException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalFile;
import org.gcube.common.homelibrary.util.WorkspaceTreeVisitor;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestWorkspace2 {

	public static void main(String[] args) {
		String user = "nikolaos.drakopoulos";
		String scope = "/gcube/devsec";

		System.out.println("TestWorkspace...");

		WorkspaceWorker worker = new WorkspaceWorker();
		Workspace w = worker.getWorkspaceWithoutASL(user,scope);
		if(w!=null)System.out.println("workspace : ok");
		else System.out.println("workspace : null");
		
		WorkspaceFolder root = w.getRoot();
		if(root!=null)System.out.println("rootFolder : ok");
		else System.out.println("rootFolder : null");
		
		//String inputUri = new String("http://upload.wikimedia.org/wikipedia/commons/6/6e/Wikipedia_logo_silver.png");
		File myFile = new File("/tmp/Wikipedia_logo_silver.png");
		InputStream streamIn=null;
		try {
			streamIn = new FileInputStream(myFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		//BROWSE
		WorkspaceTreeVisitor wtv = new WorkspaceTreeVisitor();		 
		try {
			wtv.visitVerbose(root);
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}	
		/*WorkspaceFolder folder=null;
		try {
			folder=(WorkspaceFolder)w.getItem("ff023080-dbc3-4d8d-93e0-a6dea08ae3ed");
		} catch (ItemNotFoundException e1) {
			e1.printStackTrace();
		}
		FolderDto tree=null;
		try {
			tree = worker.createTree(folder, w.getUrlWebDav());
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}		
		if(tree!=null)System.out.println("tree : ok\n\n");
		else System.out.println("tree : null");
		
		String msg = Utils.printFolder(tree, 0, "");
		//worker.printFolder(tree, 0);
		System.out.println(msg);*/
		
		//CREATE A FILE
		/*String fileName="Wikipedia_logo_silver.png";
		String type="png";
		createFile(root,fileName,streamIn,type);*/
		
		//REMOVE A FILE/FOLDER
		/*String id="a49a3897-8853-4cc8-8c65-c23afa3af6ff";
		removeItemOrFolder(w,id);	*/
		
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
