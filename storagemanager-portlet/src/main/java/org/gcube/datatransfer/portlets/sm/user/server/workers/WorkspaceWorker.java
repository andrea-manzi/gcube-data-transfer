package org.gcube.datatransfer.portlets.sm.user.server.workers;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gcube.application.framework.core.session.ASLSession;
import org.gcube.application.framework.core.session.SessionManager;
import org.gcube.contentmanagement.blobstorage.service.IClient;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.WorkspaceInitializeInfo;
import org.gcube.common.homelibrary.home.Home;
import org.gcube.common.homelibrary.home.HomeLibrary;
import org.gcube.common.homelibrary.home.HomeManagerFactory;
import org.gcube.common.homelibrary.home.exceptions.HomeNotFoundException;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItem;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItemType;
import org.gcube.common.homelibrary.home.workspace.exceptions.ExternalResourceBrokenLinkException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ExternalResourcePluginNotFoundException;
import org.gcube.common.homelibrary.home.workspace.exceptions.InsufficientPrivilegesException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemAlreadyExistException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.common.homelibrary.home.workspace.exceptions.WorkspaceFolderNotFoundException;
import org.gcube.common.homelibrary.home.workspace.folder.FolderItem;
import org.gcube.common.homelibrary.home.workspace.folder.FolderItemType;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalFile;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalImage;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalPDFFile;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalResourceLink;
import org.gcube.common.homelibrary.home.workspace.folder.items.Report;
import org.gcube.common.homelibrary.home.workspace.folder.items.ReportTemplate;
import org.gcube.common.homelibrary.home.workspace.folder.items.gcube.Document;
import org.gcube.common.homelibrary.home.workspace.folder.items.gcube.ImageDocument;
import org.gcube.common.homelibrary.home.workspace.folder.items.gcube.PDFDocument;
import org.gcube.common.homelibrary.home.workspace.folder.items.gcube.UrlDocument;
import org.gcube.common.homelibrary.home.workspace.folder.items.ts.TimeSeries;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.vomanagement.usermanagement.GroupManager;
import org.gcube.vomanagement.usermanagement.RoleManager;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.exception.GroupRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.UserManagementSystemException;
import org.gcube.vomanagement.usermanagement.exception.UserRetrievalFault;
import org.gcube.vomanagement.usermanagement.impl.liferay.LiferayGroupManager;
import org.gcube.vomanagement.usermanagement.impl.liferay.LiferayRoleManager;
import org.gcube.vomanagement.usermanagement.impl.liferay.LiferayUserManager;
import org.gcube.vomanagement.usermanagement.model.RoleModel;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.thoughtworks.xstream.XStream;

public class WorkspaceWorker extends RemoteServiceServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String USERNAME_ATTRIBUTE = "username";
	public int autoId;
	IClient clientForInputUris = null;

	public WorkspaceWorker(){
		this.autoId=0;
	}

	public ASLSession getASLSession(HttpSession httpSession)
	{
		String sessionID = httpSession.getId();
		String user = (String) httpSession.getAttribute(USERNAME_ATTRIBUTE);

		//TODO we check for the older attribute name
		if (user == null) user = (String) httpSession.getAttribute("user");
		if(user==null){System.out.println("WorkspaceWorker - getASLSession: user not found in session");return null;}
		else System.out.println("WorkspaceWorker - getASLSession: user found in session "+user);

		return SessionManager.getInstance().getASLSession(sessionID, user);
	}

	/*
	 * getUser
	 * input: Local request
	 * returns: String with the name of the user in this session
	 */
	public String getUserAndScopeAndRole(HttpServletRequest localRequest){
		try{
			if(localRequest==null){System.out.println("WorkspaceWorker - getUserAndScope: localRequest==null");return null;}

			HttpSession httpSession = localRequest.getSession();
			if(httpSession==null){System.out.println("WorkspaceWorker - getUserAndScope: httpSession==null");return null;}

			ASLSession aslSession = getASLSession(httpSession);
			if(aslSession==null)return null;	

			List<String> roles = getUserRolesByGroup(aslSession);
			String res="";
			for(String tmp:roles)res=res+tmp+"\n";
					System.out.println("WorkspaceWorker - getUserAndScope - roles:\n"+res);
					boolean isAdmin = checkAdminCase(roles);

					String name = aslSession.getUsername();
					String scope = aslSession.getScopeName();

					if(name==null || scope==null){System.out.println("WorkspaceWorker - getUserAndScope: usern or scope is null");return null;}

					//returning value .. example: nick--/gcube/devsec--false
					return name+"--"+scope+"--"+isAdmin;
		}
		catch(Exception e){
			System.out.println("WorkspaceWorker - getUserAndScope: Exception ******");
			e.printStackTrace();
			return null;
		}

	}

	public List<String> getUserRolesByGroup(ASLSession aslSession){
		try {
			List<String> roles=new ArrayList<String>();
			RoleManager roleM=new LiferayRoleManager();
			GroupManager groupM=new LiferayGroupManager();
			UserManager userM=new LiferayUserManager();
			List<RoleModel> userRolesByGroup;

			String userId = userM.getUserId(aslSession.getUsername());
			String groupId=groupM.getGroupId(aslSession.getGroupName());			

			userRolesByGroup = roleM.listRolesByUserAndGroup(groupId, userId);

			for(RoleModel tmp:userRolesByGroup)roles.add(tmp.getRoleName());
					return roles;

		} catch (UserManagementSystemException e) {
			e.printStackTrace();
			return null;
		} catch (GroupRetrievalFault e) {
			e.printStackTrace();
			return null;
		} catch (UserRetrievalFault e) {
			e.printStackTrace();
			return null;
		}	
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean checkAdminCase(List<String> roles){
		for(String role:roles){
			if(role.compareTo("VO-Admin")==0 || role.compareTo("VRE-Manager")==0){
				return true;
			}
		}
		return false;
	}

	/*
	 * getWorkspace
	 * input: HttpServletRequest 
	 * returns: String with the serialized object of workspace
	 */
	public String getWorkspace(HttpServletRequest localRequest){
		try{
			ASLSession session = getASLSession(localRequest.getSession());
			if(session==null)return null;

			Workspace w=null;
			try {
				ScopeProvider.instance.set(session.getScope()/*"/gcube"*/);
				w = HomeLibrary.getUserWorkspace(session.getUsername()/*"andrea.manzi"*/);

			} catch (WorkspaceFolderNotFoundException e) {
				e.printStackTrace();
			} catch (InternalErrorException e) {
				e.printStackTrace();
			} catch (HomeNotFoundException e) {
				e.printStackTrace();
			}
			if(w==null){System.out.println("WorkspaceWorker - getWorkspace: workspace==null");return null;}

			WorkspaceInitializeInfo workspaceInfo=new WorkspaceInitializeInfo();
			workspaceInfo.setWorkspace(w);
			String serializedWorkspace=workspaceInfo.toXML();

			if(serializedWorkspace==null){System.out.println("WorkspaceWorker - getWorkspace: serializedWorkspace==null");return null;}
			else System.out.println("WorkspaceWorker - getWorkspace: serializedObj length="+serializedWorkspace.length());

			return serializedWorkspace;		
		}catch(Exception e){
			System.out.println("WorkspaceWorker - getWorkspace: Exception ***");
			e.printStackTrace();
			return null;
		}

	}

	/*
	 * getWorkspaceWithoutSerialization
	 * input: HttpServletRequest 
	 * returns: Workspace obj
	 */
	public Workspace getWorkspaceWithoutSerialization(HttpServletRequest localRequest){
		try{
			ASLSession session = getASLSession(localRequest.getSession());
			if(session==null)return null;
			
			ScopeProvider.instance.set(session.getScope()/*"/gcube"*/);

			Workspace w=null;
			try {
				w = HomeLibrary.getUserWorkspace(session.getUsername()/*"andrea.manzi"*/);
			} catch (WorkspaceFolderNotFoundException e) {
				e.printStackTrace();
			} catch (InternalErrorException e) {
				e.printStackTrace();
			} catch (HomeNotFoundException e) {
				e.printStackTrace();
			}


			if(w==null){System.out.println("WorkspaceWorker - getWorkspaceWithoutSerialization: workspace==null");return null;}
			else return w;

		}catch(Exception e){
			System.out.println("WorkspaceWorker - getWorkspaceWithoutSerialization: Exception ***");
			e.printStackTrace();
			return null;
		}

	}

	public Workspace getWorkspaceWithoutASL(String name, String scope){
		try{

			HomeManagerFactory factory=null;
			factory = HomeLibrary.getHomeManagerFactory();
			Home home = factory.getHomeManager().getHome(name);
			Workspace w = home.getWorkspace();


			if(w==null){System.out.println("WorkspaceWorker - getWorkspaceWithoutSerialization: workspace==null");return null;}
			else return w;

		}catch(Exception e){
			System.out.println("WorkspaceWorker - getWorkspaceWithoutSerialization: Exception ***");
			e.printStackTrace();
			return null;
		}

	}

	public WorkspaceFolder getWorkspaceFolder(String serializedWorkspaceInfo, String folderId, boolean needTheParent,HttpServletRequest localRequest) throws IllegalArgumentException {
		Workspace workspace=null;

		ASLSession session = getASLSession(localRequest.getSession());
		if(session==null)return null;
		
		ScopeProvider.instance.set(session.getScope()/*"/gcube"*/);
		
		if(serializedWorkspaceInfo==null){
			workspace = getWorkspaceWithoutSerialization(localRequest);
		}
		else if(serializedWorkspaceInfo.compareTo("")==0){
			workspace = getWorkspaceWithoutSerialization(localRequest);
		}
		else {
			XStream xstream = new XStream();
			WorkspaceInitializeInfo workspaceInfo=(WorkspaceInitializeInfo)xstream.fromXML(serializedWorkspaceInfo);

			if(workspaceInfo==null){System.out.println("GET WORKSPACE FOLDER: workspaceInfo= null");return null;}
			workspace = workspaceInfo.getWorkspace();
		}
		if(workspace==null){System.out.println("GET WORKSPACE FOLDER: workspace= null");return null;}

		System.out.println("GET WORKSPACE FOLDER: folderId="+folderId+" - needparent="+needTheParent);
		WorkspaceFolder root=null;
		//String rootParent=null;
		if(folderId==null){
			root = workspace.getRoot();
		}
		else if(folderId.compareTo("")==0){
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

		if(root==null){System.out.println("GET WORKSPACE FOLDER: folder= null");return null;}

		return root;
	}

	public boolean removeItemOrFolder(String serializedWorkspaceInfo, String id,HttpServletRequest localRequest){		
		boolean error=false;
		Workspace workspace=null;

		if(serializedWorkspaceInfo==null){
			workspace = getWorkspaceWithoutSerialization(localRequest);
		}
		else if(serializedWorkspaceInfo.compareTo("")==0){
			workspace = getWorkspaceWithoutSerialization(localRequest);
		}
		else {
			XStream xstream = new XStream();
			WorkspaceInitializeInfo workspaceInfo=(WorkspaceInitializeInfo)xstream.fromXML(serializedWorkspaceInfo);

			if(workspaceInfo==null){System.out.println("REMOVE ItemOrFolder: workspaceInfo= null");return true;}
			workspace = workspaceInfo.getWorkspace();
		}
		if(workspace==null){System.out.println("REMOVE ItemOrFolder: workspace= null");return true;}

		try {
			workspace.removeItem(id);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
			error= true;
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
			error= true;
		} catch (InternalErrorException e) {
			e.printStackTrace();
			error=true;
		}
		return error;
	}

	public String createFolder(WorkspaceFolder folder,String fileName){				
		try {
			WorkspaceFolder newFolder = folder.createFolder(fileName,fileName);
			return newFolder.getId();
			
		} catch (InternalErrorException e) {
			e.printStackTrace();
		} catch (InsufficientPrivilegesException e) {
			e.printStackTrace();
		} catch (ItemAlreadyExistException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Map<String, String> uploadFiles(List<InputStream> inputStreams,List<String> inputFilenames,List<String> inputMimeTypes,WorkspaceFolder outputFolder) {
		
		Map<String, String> ret = new HashMap<String, String>();
		
		if(inputStreams==null || inputFilenames==null || inputMimeTypes==null){
			System.out.println("WorkspaceWorker - uploadFiles - some or all of the inputStreams/inputFilenames/inputMimeTypes =null");
			return ret;
		}
		if(inputStreams.size()!=inputFilenames.size() || 
				inputStreams.size()!=inputMimeTypes.size()){
			System.out.println("WorkspaceWorker - uploadFiles - length of inputStreams, inputMimeTypes, inputFilenames " +
					"must be the same...\ninputStreams.size()="+inputStreams.size()+"\ninputFilenames.size()="+inputFilenames.size()+
					"\ninputMimeTypes.size()="+inputMimeTypes.size());
			return ret;
		}

		int i =0;
		int length=inputStreams.size();
		List<WorkspaceItem> items=null;
		System.out.println("WorkspaceWorker - uploadFiles - getting the items of the specific folder");
		try {
			items=outputFolder.getChildren();
		} catch (InternalErrorException e2) {
			e2.printStackTrace();
		}
		System.out.println("WorkspaceWorker - uploadFiles - got the items of the specific folder");
		for(i=0;i<length;i++){
			InputStream streamIn=inputStreams.get(i);
			String outputName=inputFilenames.get(i);
			String mimeType=inputMimeTypes.get(i);

			WorkspaceItem foundItem=null;
			
			if(items!=null){
				for(WorkspaceItem item : items){
					String name = null;
					System.out.println("WorkspaceWorker - uploadFiles - getting the name of an item..");
					try {
						name = item.getName();
					} catch (InternalErrorException e) {
						e.printStackTrace();
					}
					System.out.println("WorkspaceWorker - uploadFiles - got the name of an item="+name+" - when outpuname="+outputName);
					if(name.compareTo(outputName)==0){
						foundItem=item;
						break;
					}
				}
			}

			if(foundItem!=null) { //already exists...
				System.out.println("WorkspaceWorker - uploadFiles - "+outputName +" already exists ...");
				System.out.println("WorkspaceWorker - uploadFiles - We replace it...");
				
				String id = replaceFile(outputFolder,foundItem,outputName,streamIn,mimeType);
				ret.put(outputName, id);
			} else{				
				System.out.println("WorkspaceWorker - uploadFiles - "+outputName+" is going to be copied..");
				ExternalFile file =null;
				try {
					file=outputFolder.createExternalFileItem(outputName,outputName, mimeType, streamIn);
					ret.put(outputName, file.getId());
				} catch (InsufficientPrivilegesException e) {
					e.printStackTrace();
				} catch (ItemAlreadyExistException e) {
					e.printStackTrace();
				} catch (InternalErrorException e) {
					e.printStackTrace();
				}	
				
				if(file!=null){
					System.out.println("WorkspaceWorker - uploadFiles - "+outputName+" has been transferred successfully");
				}
			}			
		}
		
		return ret;
	}
	public String replaceFile(WorkspaceFolder root, WorkspaceItem item,String fileName,InputStream streamIn,String type){
		
		String ret = null;
		
		ExternalFile file=null;
		if (item.getType() == WorkspaceItemType.FOLDER_ITEM) {
			//handle only externalFiles ... 
			if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_FILE) {
				file = (ExternalFile)item;
			}            
		}
		else{
			System.out.println("WorkspaceWorker - replaceFile - The file cannot be copied because there is a " +
					"folder with the same name... - Please rename from '"+fileName+"' to sth else..");
			return ret;
		}

		if(file==null){
			System.out.println("WorkspaceWorker - replaceFile - retrived file is null ...");
			return ret;
		}
		try {
			file.setData(streamIn);
		} catch (InternalErrorException e) {
			e.printStackTrace();
			System.out.println("WorkspaceWorker - uploadFiles - error during the replacement - in 'file.setData(streamIn)' ...");
			return ret;
		}

		System.out.println("WorkspaceWorker - uploadFiles - "+fileName +" successfully replaced ...");
		
		try {
			ret = file.getId();
		} catch(InternalErrorException ex) {
			ex.printStackTrace();
		}
		
		return ret;
	}

	public String fixPath(String workspaceWebDavLink, String path){
		String tmpPath=path;
		if(path.startsWith("/"))tmpPath=tmpPath.replaceFirst("/", "");
		String[] partsOfPath=tmpPath.split("/");
		String[] partsOfwebdavUrl=workspaceWebDavLink.split("/");
		String fixedRootPath="";

		//if the workspaceWebDavLink ends to 'Workspace' for example and the path starts with the same name we 
		// omit it from the path because in other case it will be double
		if(partsOfwebdavUrl[partsOfwebdavUrl.length-1].compareTo(partsOfPath[0])==0){
			for(int i=1;i<=partsOfPath.length-1;i++){
				if(i<(partsOfPath.length-1)){
					fixedRootPath=fixedRootPath+(partsOfPath[i])+"/";
				}
				else fixedRootPath=fixedRootPath+(partsOfPath[i]);
			}
			return fixedRootPath;
		}
		else return tmpPath;

	}

	/*
	 * createTree
	 * input: WorkspaceFolder 
	 * returns: A FolderDto object which is the same tree represented from the input folder
	 */
	public FolderDto createTree(WorkspaceFolder root, String workspaceWebDavLink) throws InternalErrorException {
		if(root==null){System.out.println("GET WORKSPACE MANUALLY - createTree - root is null");return null;}
		FolderDto empty = makeFolder("",null);
		FolderDto folder=null;
		List<WorkspaceItem> list=null;

		String rootPath=root.getPath();
		String fixedRootPath= fixPath(workspaceWebDavLink, rootPath);
		if(fixedRootPath.compareTo("")!=0&&!fixedRootPath.endsWith("/"))fixedRootPath=fixedRootPath+"/";

		folder = makeFolder(workspaceWebDavLink+"/"+fixedRootPath,root.getId());
		list= root.getChildren();


		if(list==null){System.out.println("GET WORKSPACE MANUALLY - createTree - list is empty");return null;}
		if(list.size()<1){
			folder.addChild(empty);
			return folder;
		}
		for(WorkspaceItem tmp : list){
			
			if(tmp.getType() == WorkspaceItemType.FOLDER || 
					tmp.getType() == WorkspaceItemType.SHARED_FOLDER ||
					tmp.getType() == WorkspaceItemType.SMART_FOLDER){
				String path=tmp.getPath();
				String fixedPath= fixPath(workspaceWebDavLink, path);

				if(fixedPath.compareTo("")!=0&&!fixedPath.endsWith("/"))fixedPath=fixedPath+"/";
				FolderDto subfolder = makeFolder(workspaceWebDavLink+"/"+fixedPath,tmp.getId());
				subfolder.addChild(empty);
				subfolder.setType(tmp.getType().name());
				subfolder.setOwner(tmp.getOwner().getPortalLogin());
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				subfolder.setLastUpdate(df.format(tmp.getLastModificationTime().getTime()));
				// TODO scale the size into KB, MB, GB ...				
				if (tmp instanceof FolderItem)
					subfolder.setSize(
							Long.toString(
									((FolderItem)tmp).getLength()
								)
						);
				
				folder.addChild(subfolder);
			}
			else if (tmp.getType() == WorkspaceItemType.FOLDER_ITEM){
				if(!checkValidity(tmp))continue; // skip this file if it cannot be transfered
				
				String path=tmp.getPath();
				String fixedPath= fixPath(workspaceWebDavLink, path);

				FolderDto child = makeFolder(workspaceWebDavLink+"/"+fixedPath,tmp.getId());
				child.setType(tmp.getType().name());
				child.setOwner(tmp.getOwner().getPortalLogin());
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				child.setLastUpdate(df.format(tmp.getLastModificationTime().getTime()));
				// TODO scale the size into KB, MB, GB ...
				if (tmp instanceof FolderItem)
					child.setSize(
							Long.toString(
									((FolderItem)tmp).getLength()
								)
						);
				
				folder.addChild(child);
			}
		}
		return folder;
	}

	//return true if this type of file can be transfered
	//else returns false
	public boolean checkValidity(WorkspaceItem item){
		InputStream inputstr=null;
		
		//in case of EXTERNAL_FILE ... 
		if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_FILE) {
			ExternalFile file = (ExternalFile)item;
			try {							
				inputstr = file.getData();
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of EXTERNAL_IMAGE ... 
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_IMAGE) {
			ExternalImage file = (ExternalImage)item;
			try {							
				inputstr = file.getData();							
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of EXTERNAL_PDF_FILE ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_PDF_FILE) {
			ExternalPDFFile file = (ExternalPDFFile)item;
			try {							
				inputstr = file.getData();							
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		} //in case of EXTERNAL_RESOURCE_LINK ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.EXTERNAL_RESOURCE_LINK) {
			ExternalResourceLink file = (ExternalResourceLink)item;
			try {							
				inputstr = file.getData();							
			} catch (InternalErrorException e) {
				e.printStackTrace();
			} catch (ExternalResourceBrokenLinkException e) {
				e.printStackTrace();
			} catch (ExternalResourcePluginNotFoundException e) {
				e.printStackTrace();
			}
		}//in case of DOCUMENT ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.DOCUMENT) {
			Document file = (Document)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of IMAGE_DOCUMENT ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.IMAGE_DOCUMENT) {
			ImageDocument file = (ImageDocument)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of PDF_DOCUMENT ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.PDF_DOCUMENT) {
			PDFDocument file = (PDFDocument)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of REPORT ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.REPORT) {
			Report file = (Report)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of REPORT_TEMPLATE ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.REPORT_TEMPLATE) {
			ReportTemplate file = (ReportTemplate)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of TIME_SERIES ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.TIME_SERIES) {
			TimeSeries file = (TimeSeries)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in case of URL_DOCUMENT ...    
		else if (((FolderItem)item).getFolderItemType() ==FolderItemType.URL_DOCUMENT) {
			UrlDocument file = (UrlDocument)item;
			try {							
				inputstr = file.getData();						
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}//in other case
		else{
			System.out.println("TransferWorker - workspace item is not a type of file you can transfer, type:"+((FolderItem)item).getFolderItemType());
			return false;
		}
		
		if(inputstr==null){
			System.out.println("TransferWorker - some inputstream is null");
			return false;
		}
		
		return true;
	}
	
	/*
	 * makeFolder
	 * input: String with the name
	 * input: String with the id in workspace
	 * returns: The created FolderDto object
	 */
	public FolderDto makeFolder(String name, String idInWorkspace) {
		FolderDto theReturn = new FolderDto(++autoId, name);
		if(idInWorkspace!=null){theReturn.setIdInWorkspace(idInWorkspace);}
		theReturn.setChildren((List<FolderDto>) new ArrayList<FolderDto>());
		return theReturn;
	}

	/*
	 * printFolder
	 * input: FolderDto
	 * input: The depth
	 * It prints the tree represented from the input folder for debugging reasons
	 */
	public void printFolder(FolderDto folder, int indent){
		for(int i = 0; i < indent; i++) System.out.print("\t");
		System.out.println("fold : name="+folder.getName() +" - id="+folder.getId()+" - idInWorkspace="+folder.getIdInWorkspace());

		List<FolderDto> tmpListOfChildren = folder.getChildren();
		if(tmpListOfChildren!=null){
			for(FolderDto tmp : tmpListOfChildren){ //first the files
				if(tmp.getChildren().size() <= 0){
					if((tmp.getName().compareTo("")==0))continue;
					for(int i = 0; i < indent; i++) System.out.print("\t");
					String type= "";
					if((tmp.getName().substring(tmp.getName().length()-1,tmp.getName().length())).compareTo("/")==0)type="fold";
					else type="file";

					System.out.println(type+" : name="+tmp.getName()+" - id="+tmp.getId());
				}
			}		    	
			for(FolderDto tmp : tmpListOfChildren){ //then the folders
				if(tmp.getChildren().size() > 0){
					printFolder(tmp,indent+1);
				}
			}
		}		    
	}

	// for testing  ... 
	public void getAttributes(HttpServletRequest localRequest){
		if(localRequest==null){System.out.println("WorkspaceWorker - getAttributes: localRequest==null");return;}
		// RenderRequest renderRequest
		HttpSession httpSession = localRequest.getSession();
		if(httpSession==null){System.out.println("WorkspaceWorker - getAttributes: httpSession==null");return;}

		String sessionID = httpSession.getId();
		System.out.println("WorkspaceWorker - getAttributes: AttributeNames of session with id="+sessionID+":");
		Enumeration em= httpSession.getAttributeNames();
		while(em.hasMoreElements()){
			String value = (String) em.nextElement();			
			System.out.println("name="+value+" - stringValue="+httpSession.getAttribute(value).toString());
		}
	}
}
