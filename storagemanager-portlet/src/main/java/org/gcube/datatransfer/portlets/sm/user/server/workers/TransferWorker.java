package org.gcube.datatransfer.portlets.sm.user.server.workers;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.vfs2.FileObject;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItem;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItemType;
import org.gcube.common.homelibrary.home.workspace.exceptions.ExternalResourceBrokenLinkException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ExternalResourcePluginNotFoundException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
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
import org.gcube.contentmanagement.blobstorage.service.IClient;
import org.gcube.contentmanager.storageclient.wrapper.AccessType;
import org.gcube.contentmanager.storageclient.wrapper.MemoryType;
import org.gcube.contentmanager.storageclient.wrapper.StorageClient;
import org.gcube.datatransfer.portlets.sm.user.server.utils.Constants;
import org.gcube.datatransfer.portlets.sm.user.server.utils.TransferUtils;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.TransferDetails;
import org.slf4j.*;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TransferWorker {
	TransferDetails transferDetailsObj;
	String sourceType;
	String destinationType;
	HttpServletRequest localRequest; // in case of not serializable workspace dest

	List<String> inputUris;
	List<InputStream> inputStreams;
	List<String> inputFilenames;

	IClient clientForInputUris;    //for smp input
	IClient clientForTheStorage;   //for MongoDB destination	
	List<String> inputMimeTypes;   //for Workspace destination
	
	Logger logger = LoggerFactory.getLogger(TransferWorker.class);

	public Map<String, String> process(TransferDetails transfDetailsObj,HttpServletRequest request){

		Map<String, String> ret = new HashMap<String, String>();
		
		inputStreams=new ArrayList<InputStream>();
		inputFilenames=new ArrayList<String>();
		inputMimeTypes=new ArrayList<String>();

		localRequest=request;
		transferDetailsObj=transfDetailsObj;	

		logger.debug("TransferWorker - setting the scope: " + transferDetailsObj.getScope());
		
		sourceType = transferDetailsObj.getSourceType();
		destinationType = transferDetailsObj.getDestinationType();

		inputUris=transferDetailsObj.getInputUrls();
		boolean errorInTheGetStreams=getInputStreamsAndFilenamesAndMimeTypes();
		if(errorInTheGetStreams){
			logger.error("TransferWorker - error during the 'getInputStreamsAndFilenamesAndMimeTypes' operation");
			return ret;
		}

		//workspace destination
		if(destinationType.equals("Workspace")){
			WorkspaceWorker wsWorker = new WorkspaceWorker();
			String destinationFolderId=transferDetailsObj.getDestinationFolderId();
			String serializedWorkspaceInfo=transferDetailsObj.getSerializedWorkspaceInfo();
			boolean needTheParent = transferDetailsObj.getNeedParent();
			WorkspaceFolder outputFolder = wsWorker.getWorkspaceFolder(serializedWorkspaceInfo, destinationFolderId, needTheParent, localRequest);
			if(outputFolder==null){
				logger.error("TransferWorker - destination folder in workspace=null");
				return ret;
			}
			ret = wsWorker.uploadFiles(inputStreams, inputFilenames, inputMimeTypes, outputFolder);
			return ret;
		}

		//mongoDB destination
		String destinationFolder = transferDetailsObj.getDestinationFolder();
		logger.debug("TransferWorker - initializeIClientForTheStorage");
		initializeIClientForTheStorage();
		logger.debug("TransferWorker - ConnectionSMP.uploadFiles");
		logger.debug("TransferWorker - scope set to: " + transferDetailsObj.getScope());
		ret = ConnectionSMP.uploadFiles(inputStreams,inputFilenames, clientForTheStorage, destinationFolder);
		return ret;		
	}

	public boolean getInputStreamsAndFilenamesAndMimeTypes(){
		//create IClient for inputs when having MongoDB as a source type
		if(sourceType.compareTo("MongoDB")==0){
			initializeIClientForInputUris();
			for(String tmp:inputUris){
				String str=tmp;
				String[] parts = str.split("\\?");			
				String[] partsOfMain=parts[0].split("/");
				String rpath=parts[0].replaceFirst("smp:/", "");
				String outputFile = partsOfMain[partsOfMain.length-1];

				InputStream input = clientForInputUris.get().RFileAsInputStream(rpath);

				if(input==null){
					logger.error("TransferWorker - some inputstream is null");
					return true;
				}
				inputStreams.add(input);	
				inputFilenames.add(outputFile);

				String mimeType= TransferUtils.guessMimeType(rpath);
				inputMimeTypes.add(mimeType);
			}
		}//Workspace as a source type
		else if(sourceType.compareTo("Workspace")==0){
			WorkspaceWorker worker = new WorkspaceWorker();
			Workspace w = worker.getWorkspaceWithoutASL(transferDetailsObj.getSubmitter(),transferDetailsObj.getScope());
			if(w==null){
				System.out.println("TransferWorker - workspace is null");
				return true;
			}			
			List<String> workspaceItemFilenames=transferDetailsObj.getInputFilenames();
			List<String> workspaceItemIds=transferDetailsObj.getInputIds();
			if(workspaceItemFilenames==null || workspaceItemIds==null){
				System.out.println("TransferWorker - workspaceItemFilenames or workspaceItemIds is null");
				return true;
			}
			else if (workspaceItemFilenames.size()!=workspaceItemIds.size()){
				System.out.println("TransferWorker - the length of workspaceItemFilenames and workspaceItemIds must be the same" +
						"\nworkspaceItemFilenames.size()="+workspaceItemFilenames.size()+"" +
						"\nworkspaceItemIds.size()="+workspaceItemIds.size());
				return true;
			}

			int i=0;
			int length = workspaceItemIds.size();
			for(i=0;i<length;i++){

				String itemId=workspaceItemIds.get(i);
				String fileName=workspaceItemFilenames.get(i);
				WorkspaceItem item=null;
				try {
					item = w.getItem(itemId);
				} catch (ItemNotFoundException e1) {
					e1.printStackTrace();
					return true;
				}
				InputStream inputstr=null;
				if (item.getType() == WorkspaceItemType.FOLDER_ITEM) {
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
						return true;
					}
				}				
				else {
					System.out.println("TransferWorker - workspace item is not a folder_item");
					return true;
				}

				if(inputstr==null){
					System.out.println("TransferWorker - inputstream of item with id="+itemId+" is null");
					return true;
				}
				inputStreams.add(inputstr);
				inputFilenames.add(fileName);

				String mimeType= TransferUtils.guessMimeType(fileName);
				inputMimeTypes.add(mimeType);				
			}
		}//other source type
		else{
			for(String tmp:inputUris){
				try {
					
					FileObject inputFile = TransferUtils.prepareFileObject(tmp);
					if(inputFile==null){
						throw new Exception("inputFile=null");
					}
					else if(inputFile.getURL()==null)throw new Exception("inputFile.getURL()=null");

					URLConnection connection = inputFile.getURL().openConnection();
					connection.setConnectTimeout(Constants.defaultTimeOut);
					InputStream inputstr = connection.getInputStream();
					String fileName;
					if(inputFile.getURL().toString().startsWith("smp")){			
						String str=tmp;
						String[] parts = str.split("\\?");
						String[] partsOfMain=parts[0].split("/");
						fileName = partsOfMain[partsOfMain.length-1];
					}
					else fileName= inputFile.getName().getBaseName();

					inputStreams.add(inputstr);
					inputFilenames.add(fileName);

					String mimeType= TransferUtils.guessMimeType(fileName);
					inputMimeTypes.add(mimeType);						
				}
				catch(Exception e){
					e.printStackTrace();
					return true;
				}
			}
		}
		return false;
	}


	public void initializeIClientForTheStorage(){
		try {
			logger.debug("TransferWorker - setting scope: " + transferDetailsObj.getScope());
			ScopeProvider.instance.set(transferDetailsObj.getScope());
			logger.debug("TransferWorker - scope set to: " + transferDetailsObj.getScope());
			clientForTheStorage = new StorageClient(
					transferDetailsObj.getServiceClass(), 
					transferDetailsObj.getServiceName(),
					transferDetailsObj.getOwner(),
					AccessType.valueOf(transferDetailsObj.getAccessType()),
					MemoryType.valueOf(transferDetailsObj.getDstAreaType().toUpperCase())
				).getClient();

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("TransferWorker - StorageClient details: \n" +
				"ServiceClass="+transferDetailsObj.getServiceClass()+"\n"+
				"ServiceName="+transferDetailsObj.getServiceName()+"\n"+
				"Owner="+transferDetailsObj.getOwner()+"\n"+
				"AccessType="+transferDetailsObj.getAccessType()+"\n"+
				"Scope="+transferDetailsObj.getScope()+"\n");
	}

	public void initializeIClientForInputUris(){
		try {
			ScopeProvider.instance.set(transferDetailsObj.getScope());

			clientForInputUris = new StorageClient(
					transferDetailsObj.getServiceClassSource(), 
					transferDetailsObj.getServiceNameSource(),
					transferDetailsObj.getOwnerSource(),
					AccessType.valueOf(transferDetailsObj.getAccessTypeSource()),
					MemoryType.valueOf(transferDetailsObj.getSrcAreaType().toUpperCase())
				).getClient();
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("TransferWorker - StorageClient For Input Uris details: \n" +
				"ServiceClass Source="+transferDetailsObj.getServiceClassSource()+"\n"+
				"ServiceName Source="+transferDetailsObj.getServiceNameSource()+"\n"+
				"Owner Source="+transferDetailsObj.getOwnerSource()+"\n"+
				"AccessType Source="+transferDetailsObj.getAccessTypeSource()+"\n"+
				"Scope="+transferDetailsObj.getScope()+"\n");
	}

}
