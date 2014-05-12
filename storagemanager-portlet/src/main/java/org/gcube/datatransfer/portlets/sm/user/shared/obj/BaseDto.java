package org.gcube.datatransfer.portlets.sm.user.shared.obj;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.TreeStore.TreeNode;

public class BaseDto implements  JsonSerializable , TreeStore.TreeNode<BaseDto> {

  private int id;
  private String name;  //path
  private String shortname;
  private String idInWorkspace;   //if it's for workspace items
  private String parentIdInWorkspace;  //if it's for workspace items
  
  // files attributes
  private String owner;
  private String type;
  private String lastUpdate;
  private String size;

  private String link;

  public BaseDto() {    
  
  }
  public BaseDto(Integer id, String name) {
	    this.id = id;
	    this.name = name;
	    this.idInWorkspace=null;
	    this.parentIdInWorkspace=null;
	    
	    String[] nameParts=name.split("/");
		if(nameParts.length>1)this.shortname= nameParts[nameParts.length-1];
		else this.shortname=name;
  }
  
  public static Serializer createSerializer(){
	   return GWT.create(Serializer.class);
}


public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
public String getShortname() {
	return shortname;
}
public void setShortname(String shortname) {
	this.shortname = shortname;
}
public BaseDto getData() {
    return this;
  }

  public List< ? extends TreeNode<BaseDto> > getChildren() {
    return null;
  }
  
  @Override
  public String toString() {
    return name != null ? name : super.toString();
  }
public String getIdInWorkspace() {
	return idInWorkspace;
}
public void setIdInWorkspace(String idInWorkspace) {
	this.idInWorkspace = idInWorkspace;
}
public String getLink() {
	return link;
}
public void setLink(String link) {
	this.link = link;
}
public String getParentIdInWorkspace() {
	return parentIdInWorkspace;
}
public void setParentIdInWorkspace(String parentIdInWorkspace) {
	this.parentIdInWorkspace = parentIdInWorkspace;
}


public void setOwner(String owner) {
	this.owner = owner;
}

public String getOwner() {
	if (owner == null) return "n/a";
	return owner;
}

public void setType(String type) {
	this.type = type;
}

public String getType() {
	if (type == null) return "n/a";
	return type;
}

public void setLastUpdate(String lastUpdate) {
	this.lastUpdate = lastUpdate;
}

public String getLastUpdate() {
	if (lastUpdate == null) return "n/a";
	return lastUpdate;
}

public void setSize(String size) {
	this.size = size;
}

public String getSize() {
	if (size == null) return "n/a";
	return size;
}

}
