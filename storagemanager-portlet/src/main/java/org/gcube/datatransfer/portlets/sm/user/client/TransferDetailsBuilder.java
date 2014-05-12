package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.TransferDetails;

import com.google.gwt.i18n.shared.DateTimeFormat;

public class TransferDetailsBuilder {

	public TransferDetailsBuilder(BaseDto destinationFolder, List<? extends BaseDto> items) {
		
		this.items = items;
		
		// create an instance
		instance = new TransferDetails();
		
		// set common parameters
		instance.setSubmitter(Common.resourceName);
		instance.setScope(Common.scope);
		
		// set submission time
		DateTimeFormat formatter = DateTimeFormat.getFormat("dd.MM.yy-HH.mm");
		instance.setSubmittedDate(
				formatter.format(new Date())
			);
		
		// set destination folder
		this.destinationFolder = destinationFolder;
	}
	
	public void buildMongoDBDestinationFolder() {
		
		// set the destination folder
		String path = destinationFolder == null ? "/" : destinationFolder.getName();
		instance.setDestinationFolder(path);
	}
	
	public void buildWorkspaceDestinationFolder() {
		
		// set the destination folder
		String id = destinationFolder == null ? null : destinationFolder.getIdInWorkspace();
		instance.setDestinationFolderId(id);
		instance.setNeedParent(false);
	}
	
	public void buildMongoDBSourceFiles() {
		// set files that shall be transfered
		List<String> inputUrls = new ArrayList<String>();
		for (BaseDto tmp : items) {
			
			if (tmp.getName().isEmpty()) continue;
			
			if (tmp.getChildren() != null) {
				if (tmp.getChildren().size() == 1 && tmp.getChildren().get(0).getData().getName().isEmpty())
					continue;
			}
			
			String str = tmp.getLink();
			if (str != null && !str.startsWith("smp://")) {
				str = str.replaceFirst("smp:/", "smp://");
			}
			
			inputUrls.add(str);
		}
		instance.setInputUrls(inputUrls);		
	}
	
	public void buildWorkspaceSourceFiles() {
		// set files that shall be transfered
		List<String> inputUrls = new ArrayList<String>();
		List<String> inputIds = new ArrayList<String>();
		List<String> inputFilenames = new ArrayList<String>();
		
		for (BaseDto tmp : items) {
			
			if (tmp.getName().isEmpty()) continue;
			
			if (tmp.getChildren() != null) {
				if (tmp.getChildren().size() == 1 && tmp.getChildren().get(0).getData().getName().isEmpty())
					continue;
			}

			inputUrls.add(tmp.getName());
			inputIds.add(tmp.getIdInWorkspace());
			inputFilenames.add(tmp.getShortname());
		}	
		
		instance.setInputUrls(inputUrls);
		instance.setInputIds(inputIds);
		instance.setInputFilenames(inputFilenames);
	}
	
	// both for URI and Upload
	public void buildExternalSourceFiles() {
		// set files that shall be transfered
		List<String> inputUrls = new ArrayList<String>();
		for (BaseDto tmp : items) {
			
			if (tmp.getName().isEmpty()) continue;
			
			if (tmp.getChildren() != null) {
				if (tmp.getChildren().size() == 1 && tmp.getChildren().get(0).getData().getName().isEmpty())
					continue;
			}
			
			inputUrls.add(tmp.getName());
		}
		instance.setInputUrls(inputUrls);
	}
	
	public void buildMongoDbSourceType() {
		
		instance.setSourceType("MongoDB");
	}
	
	public void buildWorkspaceSourceType() {
		
		instance.setSourceType("Workspace");
	}
	
	public void buildUploadSourceType() {
		
		instance.setSourceType("Upload");
	}
	
	
	public void buildUriSourceType() {
		
		instance.setSourceType("URI");
	}
	
	public void buildMongoDBSourceParameters(Side side) {

		PopUpMongoDB params = PopUpMongoDB.getInstance(side);
		
		instance.setServiceClassSource(params.get(ServiceParameter.CLASS));
		instance.setServiceNameSource(params.get(ServiceParameter.NAME));
		instance.setAccessTypeSource(params.get(ServiceParameter.ACCESS_TYPE));
		instance.setOwnerSource(params.get(ServiceParameter.OWNER));
		instance.setSrcAreaType(params.get(ServiceParameter.AREA_TYPE));
	}
	
	public void buildMongoDbDestinationType() {
		
		instance.setDestinationType("MongoDB");
	}

	public void buildMongoDbDestinationParameters(Side side) {
		
		PopUpMongoDB params = PopUpMongoDB.getInstance(side);
		
		instance.setServiceClass(params.get(ServiceParameter.CLASS));
		instance.setServiceName(params.get(ServiceParameter.NAME));
		instance.setAccessType(params.get(ServiceParameter.ACCESS_TYPE));
		instance.setOwner(params.get(ServiceParameter.OWNER));
		instance.setDstAreaType(params.get(ServiceParameter.AREA_TYPE));
	}
	
	public void buildWorkspaceDestinationType() {
		
		instance.setDestinationType("Workspace");
	}
	
	public TransferDetails get() {
		return instance;
	}
	
	private TransferDetails instance;
	private List<? extends BaseDto> items;
	private BaseDto destinationFolder;
}
