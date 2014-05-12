package org.gcube.datatransfer.portlets.sm.user.client.utils;

import java.util.ArrayList;
import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;

import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.data.shared.IconProvider;
import com.sencha.gxt.examples.resources.client.images.ExampleImages;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;


/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class Utils {
	private int autoId;

	public Utils(){
		autoId=0;
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
	 * createAnchor input: String with the message -- returns: ToolTipConfig It
	 * creates a new anchor which contains the input string message
	 */
	public ToolTipConfig createAnchor(String message) {
		ToolTipConfig config = new ToolTipConfig();
		config.setBodyText(message);
		// config.setMouseOffset(new int[]{0,0});
		// config.setAnchor(Side.LEFT);
		// config.setCloseable(true);
		config.setTrackMouse(true);
		return config;
	}

	public static String keepOnlyTheLastPart(String name){
		String tmp=name;

		if(tmp.endsWith("/"))tmp=tmp.substring(0, tmp.length()-1);
		if(tmp.endsWith("\\"))tmp=tmp.substring(0, tmp.length()-1);

		if(tmp.contains("/")){
			//for regular path		
			String[] parts = tmp.split("/");
			if(parts.length>0){
				tmp=parts[parts.length-1];
				return tmp;
			}
		}else if(tmp.contains("\\")){
			//for windows path
			String[] parts2 = tmp.split("\\\\");
			if(parts2.length>0){
				tmp=parts2[parts2.length-1];
				return tmp;
			}
		}
		return tmp;
	}
	public static String getParentPath(String currentPath){
		if(currentPath==null)return null;
		else if(currentPath.compareTo("")==0)return null;
		
		if(currentPath.endsWith("/"))currentPath=currentPath.substring(0,currentPath.length()-1);
		
		int pos = currentPath.lastIndexOf("/");
		if(pos!=0)return currentPath.substring(0,pos);
		else if(pos==0)return "/";
		else return currentPath;
	}
	
	public static String msg="";
	public static void printFolder(FolderDto folder, int indent){
		for(int i = 0; i < indent; i++) msg=msg+("\t");
		msg=msg+("fold : name="+folder.getName() +" - id="+folder.getId()+" - idInWorkspace="+folder.getIdInWorkspace());
		msg=msg+"\n";
		List<FolderDto> tmpListOfChildren = folder.getChildren();
		if(tmpListOfChildren!=null){
			for(FolderDto tmp : tmpListOfChildren){ //first the files
				if(tmp.getChildren().size() <= 0){
					if((tmp.getName().compareTo("")==0))continue;
					for(int i = 0; i < indent; i++) System.out.print("\t");
					String type= "";
					if((tmp.getName().substring(tmp.getName().length()-1,tmp.getName().length())).compareTo("/")==0)type="fold";
					else type="file";

					msg=msg+(type+" : name="+tmp.getName()+" - id="+tmp.getId());
					msg=msg+"\n";
				}
			}		    	
			for(FolderDto tmp : tmpListOfChildren){ //then the folders
				if(tmp.getChildren().size() > 0){
					printFolder(tmp,indent+1);
				}
			}
		}	
	}
	
	public static final IconProvider<BaseDto> iconProvider = new IconProvider<BaseDto>() {				
		@Override
		public ImageResource getIcon(BaseDto model) {

			String fname = model.getName().toLowerCase();
			return getMyImage(fname);
		}
	};
	
	public static ImageResource getMyImage(String fname){
		
		if(fname == null || fname.endsWith("/")) return null;
		else if(fname.endsWith(".xml"))return ExampleImages.INSTANCE.xml();
		else if (fname.endsWith(".css"))return ExampleImages.INSTANCE.css();
		else if (fname.endsWith(".html"))return ExampleImages.INSTANCE.html();
		else if (fname.endsWith(".java"))return ExampleImages.INSTANCE.java();
		else return ExampleImages.INSTANCE.text();
	}
	
}
