package org.gcube.datatransfer.portlets.sm.user.test;

import org.gcube.datatransfer.portlets.sm.user.server.workers.WorkspaceWorker;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TestWorkspace {
	
	public static void main(String[] args) {
		String name = "nikolaos.drakopoulos";
		String scope = "/gcube/devsec";
		
		System.out.println("TestWorkspace...");
		
		WorkspaceWorker worker = new WorkspaceWorker();
		Workspace w = worker.getWorkspaceWithoutASL(name,scope);
		if(w!=null)System.out.println("workspace : ok");
		else System.out.println("workspace : null");
		
		WorkspaceFolder root = w.getRoot();
		if(root!=null)System.out.println("rootFolder : ok");
		else System.out.println("rootFolder : null");
		
		String workspaceWebDavLink=null;
		try {
			workspaceWebDavLink=w.getUrlWebDav();
		} catch (InternalErrorException e1) {
			e1.printStackTrace();
		}
		if(workspaceWebDavLink!=null){
			System.out.println("workspaceWebDavLink : ok");
			System.out.println("workspaceWebDavLink : "+workspaceWebDavLink);
		}
		else System.out.println("workspaceWebDavLink : null");

		FolderDto tree=null;
		try {
			tree = worker.createTree(root, workspaceWebDavLink);
		} catch (InternalErrorException e) {
			e.printStackTrace();
		}
		
		if(tree!=null)System.out.println("tree : ok\n\n");
		else System.out.println("tree : null");

		worker.printFolder(tree, 0);		
	}

}
