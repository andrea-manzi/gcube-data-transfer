package org.gcube.datatransfer.portlets.sm.user.shared.obj;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TransferDetails  implements JsonSerializable{

	//user name - scope
	public String submitter;
	public String scope;

	//source
	public List<String> inputIds;       //in case of workspace
	public List<String> inputFilenames; //in case of workspace
	public List<String> inputUrls;		//in other case

	public String sourceType;

	//destination
	public String destinationType;
	public String destinationFolder; //in case of mongoDB
	public String destinationFolderId;//in case of workspace destination
	public boolean needParent; 
	public String serializedWorkspaceInfo;

	//for StorageManagerDetails in the destination side
	public String accessType;
	public String owner;
	public String serviceClass;
	public String serviceName;

	//for StorageManagerDetails in the source side
	public String accessTypeSource;
	public String ownerSource;
	public String serviceClassSource;
	public String serviceNameSource;
	
	public String srcAreaType = "";
	public String dstAreaType = "";

	//authentication workspace
	String pass;

	//"dd.MM.yy-HH.mm"
	String submittedDate; 

	public TransferDetails(){
		this.submitter = "";
		this.scope = "";
		this.inputUrls = new ArrayList<String>();
		this.inputIds = new ArrayList<String>();
		this.inputFilenames = new ArrayList<String>();
		this.destinationFolderId= "";
		this.destinationFolder = "";
		this.destinationType = "";
		
		this.accessType = "";		
		this.owner = "";
		this.serviceClass = "";
		this.serviceName = "";
		this.accessTypeSource = "";
		this.ownerSource = "";
		this.serviceClassSource = "";
		this.serviceNameSource = "";
		
		this.pass="";
		this.sourceType="";
		this.submittedDate="";
		this.needParent=false;
		this.serializedWorkspaceInfo="";
	}

	public static Serializer createSerializer(){
		return GWT.create(Serializer.class);
	}

	public String getSubmitter() {
		return submitter;
	}

	public String getScope() {
		return scope;
	}

	public List<String> getInputUrls() {
		return inputUrls;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public String getAccessType() {
		return accessType;
	}

	public String getOwner() {
		return owner;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getPass() {
		return pass;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setInputUrls(List<String> inputUrls) {
		this.inputUrls = inputUrls;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(String submittedDate) {
		this.submittedDate = submittedDate;
	}

	public String getDestinationType() {
		return destinationType;
	}
	public void setDestinationType(String destinationType) {
		this.destinationType = destinationType;
	}

	public String getDestinationFolderId() {
		return destinationFolderId;
	}

	public void setDestinationFolderId(String destinationFolderId) {
		this.destinationFolderId = destinationFolderId;
	}

	public boolean getNeedParent() {
		return needParent;
	}

	public String getSerializedWorkspaceInfo() {
		return serializedWorkspaceInfo;
	}

	public void setNeedParent(boolean needParent) {
		this.needParent = needParent;
	}

	public void setSerializedWorkspaceInfo(String serializedWorkspaceInfo) {
		this.serializedWorkspaceInfo = serializedWorkspaceInfo;
	}

	public String getAccessTypeSource() {
		return accessTypeSource;
	}

	public String getOwnerSource() {
		return ownerSource;
	}

	public String getServiceClassSource() {
		return serviceClassSource;
	}

	public String getServiceNameSource() {
		return serviceNameSource;
	}

	public void setAccessTypeSource(String accessTypeSource) {
		this.accessTypeSource = accessTypeSource;
	}

	public void setOwnerSource(String ownerSource) {
		this.ownerSource = ownerSource;
	}

	public void setServiceClassSource(String serviceClassSource) {
		this.serviceClassSource = serviceClassSource;
	}

	public void setServiceNameSource(String serviceNameSource) {
		this.serviceNameSource = serviceNameSource;
	}

	public List<String> getInputIds() {
		return inputIds;
	}

	public List<String> getInputFilenames() {
		return inputFilenames;
	}

	public void setInputIds(List<String> inputIds) {
		this.inputIds = inputIds;
	}

	public void setInputFilenames(List<String> inputFilenames) {
		this.inputFilenames = inputFilenames;
	}
	
	public void setSrcAreaType(String s) {
		this.srcAreaType = s;
	}
	
	public void setDstAreaType(String s) {
		this.dstAreaType = s;
	}
	
	public String getSrcAreaType() {
		return srcAreaType;
	}
	
	public String getDstAreaType() {
		return dstAreaType;
	}


}
