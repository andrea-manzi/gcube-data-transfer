package org.gcube.datatransfer.portlets.sm.user.client.popup;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.client.Common;
import org.gcube.datatransfer.portlets.sm.user.client.GuiStateHolder;
import org.gcube.datatransfer.portlets.sm.user.client.Side;
import org.gcube.datatransfer.portlets.sm.user.client.StorageManagerPortlet;
import org.gcube.datatransfer.portlets.sm.user.client.StorageType;
import org.gcube.datatransfer.portlets.sm.user.client.TreeLoaderBuilder;
import org.gcube.datatransfer.portlets.sm.user.client.obj.Uri;
import org.gcube.datatransfer.portlets.sm.user.client.prop.UriProperties;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridRowEditing;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN), Michał Simon (CERN)
 *
 */

public class PopUpUris extends FocusPanel{

	static public PopUpUris getInstance(Side side) {
		
		// get the instance
		PopUpUris instance = instances.get(side);
		// if it's null vrate it
		if (instance == null) {
			instance = new PopUpUris(side);
			instances.put(side, instance);
		}
		
		return instance;		
	}
	
	private GridEditing<Uri> createGridEditing(Grid<Uri> editableGrid) {
		
		return new GridRowEditing<Uri>(editableGrid);
	}
	
	private PopUpUris(Side side) {
		
		this.side = side;
		create();
		setSomeDefaultUris();
	}
	
	private void create() {
		
		FramedPanel panel = new FramedPanel();
		panel.setHeadingText("URI's");
		panel.addStyleName("margin-10");
		panel.setPixelSize(400, 250);

		cc1Uris = new ColumnConfig<Uri, String>(uriProp.name(), 60, "Name");
		cc2Uris = new ColumnConfig<Uri, String>(uriProp.URI(), 150, "URI");

		List<ColumnConfig<Uri, ?>> l = new ArrayList<ColumnConfig<Uri, ?>>();
		l.add(cc1Uris);
		l.add(cc2Uris);
		cm = new ColumnModel<Uri>(l);
		
		if (storeForUris == null)
			storeForUris = new ListStore<Uri>(uriProp.key());

		uriGrid = new Grid<Uri>(storeForUris, cm);
		uriGrid.getView().setAutoExpandColumn(cc1Uris);
		editing = createGridEditing(uriGrid);
		editing.addEditor(cc1Uris, new TextField());
		editing.addEditor(cc2Uris, new TextField());

		ToolBar toolBar = new ToolBar();
		TextButton clear = new TextButton("Clear all");
		clear.setBorders(true);
		clear.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				editing.cancelEditing();
				storeForUris.clear();
				storeForUris.commitChanges();
			}
		});
		toolBar.add(clear);

		TextButton add = new TextButton("Add URI");
		add.setBorders(true);
		add.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				Uri uri = new Uri();
				uri.setName("Uri Name Example");
				uri.setURI("http://example.gr");
				editing.cancelEditing();
				storeForUris.add(0, uri);
				editing.startEditing(new GridCell(0, 0));
			}
		});
		toolBar.add(add);

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(true);
		con.setHeight("185px");
		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.add(uriGrid, new VerticalLayoutData(1, 1));

		con.setWidth("300px");

		// ************** buttons **************
		TextButton addButton = new TextButton("Add");
		addButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				onAdd();
			}
		});
		TextButton cancelButton = new TextButton("Cancel");
		cancelButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				StorageManagerPortlet.instance.mainDialogBox.hide();
				StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
				
				Info.display("Message",
						"You have not added any URIS for transfer");
			}
		});

		panel.setWidget(con);
		panel.setButtonAlign(BoxLayoutPack.START);
		panel.addButton(cancelButton);
		panel.addButton(addButton);

		uriGrid.setSelectionModel(new CellSelectionModel<Uri>());
		uriGrid.getColumnModel().getColumn(0).setHideable(false);

		// key handlers -------------
		this.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {

					StorageManagerPortlet.instance.mainDialogBox.hide();
					StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
					
					Info.display("Message",
							"You have not added any URIS for transfer");
				} else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					onAdd();
				}
			}
		});
		this.add(panel);
		// --------------
	}
		
	private void onAdd(){
		
		StorageManagerPortlet.instance.sourceAndDestination.setStorageType(side, StorageType.URI);
		
		StorageManagerPortlet.instance.mainDialogBox.hide();
		StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
		storeForUris.commitChanges();

		if (storeForUris.getAll().isEmpty()) {
			Info.display("Message",
					"You have not added any URIS for transfer");
		} else {
			
			List<BaseDto> ret = new ArrayList<BaseDto>();
			
			for (Uri tmp : storeForUris.getAll()) {
				
				BaseDto dto = StorageManagerPortlet.instance.utils.makeFolder(tmp.getURI(),null);
				
				if (tmp.getURI().startsWith("smp://")) {
					
					String str=tmp.getURI();
					String[] parts = str.split("\\?");
					String[] partsOfMain=parts[0].split("/");
					dto.setShortname(partsOfMain[partsOfMain.length-1]);
				}
			
				ret.add(dto);
			}
			
			TreeGrid<BaseDto> tree = GuiStateHolder.tree(side);
			TreeLoaderBuilder builder = new TreeLoaderBuilder(tree);
			builder.buildNoServiceProxy();
			builder.buildListReader(ret);
			TreeLoader<BaseDto> loader = builder.get();
			GuiStateHolder.setLoader(side, loader);
			tree.setTreeLoader(loader);
			loader.load();
			GuiStateHolder.disableDropping(side);
			GuiStateHolder.disableMenu(side);
		}
	}
	
	
	private void setSomeDefaultUris(){		
		
		storeForUris = new ListStore<Uri>(uriProp.key());
		Uri temp = new Uri();
		temp.setName("WikiPhoto1");
		temp.setURI("http://upload.wikimedia.org/wikipedia/commons/6/6e/Wikipedia_logo_silver.png");
		temp.setToBeTransferred(true);
		storeForUris.add(temp);
		temp = new Uri();
		temp.setName("WikiPhoto2");
		temp.setURI("http://upload.wikimedia.org/wikipedia/commons/0/0c/Fira_at_Santorini_%28from_north%29.jpg");
		temp.setToBeTransferred(true);
		storeForUris.add(temp);
		temp = new Uri();
		temp.setName("BigFile.iso");
		temp.setURI("http://ftp.lip6.fr/pub/linux/distributions/scientific/6.0/x86_64/iso/SL-60-x86_64-2011-03-03-Everything-DVD2.iso");
		temp.setToBeTransferred(true);
		storeForUris.add(temp);		
		
		temp = new Uri();
		temp.setName("CostaRica.jpg");
		temp.setURI("smp://img/CostaRica.jpg?5ezvFfBOLqaqBlwCEtAvz4ch5BUu1ag3yftpCvV+gayz9bAtSsnO1/sX6pemTKbDe0qbchLexXe8yVPqyEgEX301SBxBimaW2cbB+i5RUOH+ENSe5RIsJFOBij7Ig+jw454QKrFu2pZ1otfPLFcJou75KcG84WGNwZWfahm0GasaZs/n4coLNw==");
		temp.setToBeTransferred(true);
		storeForUris.add(temp);		
		
		storeForUris.commitChanges();
		uriGrid.reconfigure(storeForUris, cm);
	}
		
	public void popUpNow(){
		Common.foc=this;
		StorageManagerPortlet.instance.mainDialogBox.popUpSomePanel(this);
	}
	
	// map with instances
	static private Map<Side, PopUpUris> instances = new EnumMap<Side, PopUpUris>(Side.class); 
	
	// GUI components
	private UriProperties uriProp = GWT.create(UriProperties.class);
	private Grid<Uri> uriGrid;
	private ListStore<Uri> storeForUris;
	private GridEditing<Uri> editing;
	private ColumnConfig<Uri, String> cc1Uris;
	private ColumnConfig<Uri, String> cc2Uris;
	private ColumnModel<Uri> cm;
	
	// left or right
	private Side side;
}
