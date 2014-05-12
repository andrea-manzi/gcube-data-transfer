package org.gcube.datatransfer.portlets.sm.user.client.popup;

import java.util.EnumMap;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.client.Common;
import org.gcube.datatransfer.portlets.sm.user.client.GuiStateHolder;
import org.gcube.datatransfer.portlets.sm.user.client.Side;
import org.gcube.datatransfer.portlets.sm.user.client.StorageManagerPortlet;
import org.gcube.datatransfer.portlets.sm.user.client.StorageType;
import org.gcube.datatransfer.portlets.sm.user.client.TreeLoaderBuilder;
import org.gcube.datatransfer.portlets.sm.user.client.utils.Utils;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent.SubmitCompleteHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FileUploadField;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.FormPanel.Encoding;
import com.sencha.gxt.widget.core.client.form.FormPanel.Method;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN), Michał Simon (CERN)
 *
 */

public class PopUpUploadFile extends FocusPanel {
	
	static public PopUpUploadFile getInstance(Side side) {
		
		// get the instance
		PopUpUploadFile instance = instances.get(side);
		// if it's null vrate it
		if (instance == null) {
			instance = new PopUpUploadFile(side);
			instances.put(side, instance);
		}
		
		return instance;		
	}
	
	private PopUpUploadFile(Side side){

		this.side = side;
		create();
	}

	private void create(){
		panel = new FramedPanel();
		panel.setHeadingText("File Upload");
		panel.setButtonAlign(BoxLayoutPack.START);
		panel.setWidth(500);
		panel.setBodyStyle("background: none; padding: 5px");

		form = new FormPanel();
		form.setAction(GWT.getModuleBaseURL() + "fileupload");
		form.setEncoding(Encoding.MULTIPART);
		form.setMethod(Method.POST);
		panel.add(form);

		VerticalLayoutContainer p = new VerticalLayoutContainer();
		form.add(p);

		firstName = new TextField();
		firstName.setName("filename");
		firstName.setAllowBlank(false);
		p.add(new FieldLabel(firstName, "Name"), new VerticalLayoutData(-18, -1));

		username = new TextField();
		username.setValue(Common.resourceName);
		username.setName("username");
		username.setAllowBlank(false);
		FieldLabel usernameLabel=new FieldLabel(username, "Username");
		p.add(usernameLabel, new VerticalLayoutData(-18, -1));
		
		username.hide();
		usernameLabel.hide();

		
		final FileUploadField file = new FileUploadField();
		file.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Info.display("File Changed", "You selected " + file.getValue());
				String fileName=Utils.keepOnlyTheLastPart(file.getValue());
				firstName.setValue(fileName);
			}
		});
		file.setName("uploadedfile");
		file.setAllowBlank(false);

		p.add(new FieldLabel(file, "File"), new VerticalLayoutData(-18, -1));

		TextButton cancel = new TextButton("Cancel");
		cancel.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				StorageManagerPortlet.instance.mainDialogBox.hide();
				StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
			}
		});
		panel.addButton(cancel);
		
		TextButton btn = new TextButton("Reset");
		btn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				form.reset();
				file.reset();
			}
		});

		panel.addButton(btn);

		btn = new TextButton("Submit");
		btn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				onSubmit();
			}
		});
		panel.addButton(btn);

		// Add a SubmitCompleteHandler to the form.
        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {			
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
			}
		});
        
        
		// key handlers -------------
		this.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					StorageManagerPortlet.instance.mainDialogBox.hide();
					StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
					Info.display("Message",
							"You have not uploaded any files for transfer");
				} else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					onSubmit();
				}
			}
		});
		this.add(panel);
		// --------------
	}

	public void onSubmit(){
		
		if (!form.isValid()) {
			Info.display("warning","check the fields again...");
			return;
		}
		
		StorageManagerPortlet.instance.sourceAndDestination.setStorageType(side, StorageType.UPLOAD);
		
		form.submit();
		StorageManagerPortlet.instance.mainDialogBox.hide();
		StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();

		Timer getLocalFolderTimer= new Timer() {
			
			@Override
			public void run() {
				TreeGrid<BaseDto> tree = GuiStateHolder.tree(side);
				TreeLoaderBuilder builder = new TreeLoaderBuilder(tree);
				builder.buildUploadProxy();
				builder.buildServiceReader();
				TreeLoader<BaseDto> loader = builder.get();
				GuiStateHolder.setLoader(side, loader);
				tree.setTreeLoader(loader);
				loader.load();
				GuiStateHolder.disableDropping(side);
				GuiStateHolder.disableMenu(side);
			}
		};
		
		getLocalFolderTimer.schedule(2000);
	}
	
	public void refreshUsername(){
		this.username.setValue(Common.resourceName);
	}
	public void popUpNow(){
		Common.foc=this;
		StorageManagerPortlet.instance.mainDialogBox.popUpSomePanel(this);
	}
	
	// map with instances
	static private Map<Side, PopUpUploadFile> instances = new EnumMap<Side, PopUpUploadFile>(Side.class); 
	
	// GUI components
	private FramedPanel panel ;
	private FormPanel form;
	private TextField firstName ,username;
	
	// left or right
	private Side side;
}
