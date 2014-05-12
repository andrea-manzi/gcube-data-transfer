package org.apache.commons.vfs2.provider.sdsc;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.UriParser;

import edu.sdsc.grid.io.GeneralFileSystem;
import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.RemoteFile;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 5, 2008
 * Time: 8:23:07 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class SDSCFileObject extends AbstractFileObject
{
    protected RemoteFile file;
    protected GeneralFileSystem fileSystem;

    /**
     * The key used to get/set the FILE_SIZE property for this object using the SRBFileObject.getAttribute(..) SRBFileObject.setAttribute(..) functions
     */
    public final static String FILE_SIZE = "FILE_SIZE";

    /**
     * The key used to get/set the FILE_TYPE property for this object using the SRBFileObject.getAttribute(..) SRBFileObject.setAttribute(..) functions
     */
    public final static String FILE_TYPE = "FILE_TYPE";

    /**
     * The key used to get/set the LAST_MODIFIED property for this object using the SRBFileObject.getAttribute(..) SRBFileObject.setAttribute(..) functions
     */
    public final static String LAST_MODIFIED = "LAST_MODIFIED";

    /**
     * The key used to get/set the SRB_RESOURCE property for this object using the SRBFileObject.getAttribute(..) SRBFileObject.setAttribute(..) functions
     */
    public final static String RESOURCE = "RESOURCE";

    /**
     * The key used to get/set the ACCESS_CONSTRAINT property for this object using the SRBFileObject.getAttribute(..) SRBFileObject.setAttribute(..) functions
     */
    public final static String ACCESS_CONSTRAINT = "ACCESS_CONSTRAINT";

    /**
     * The key used to get/set the PRE_CONFIGURED property for this object using the SRBFileObject.getAttribute(..) SRBFileObject.setAttribute(..) functions
     */
    public final static String PRE_CONFIGURED = "PRE_CONFIGURED";
    

    /**
     * Holds the cached attributes for this object, making them fast to get
     */
    protected Map fileAttributes;

    /**
     * Is populated by the doListChildrenResolved method, to hold the children objects of this object if it is a directory
     */
    protected Map children;

    /**
     * The VFS absolute path to this object, used to resolve it
     */
    protected String filePath;

    /**
     * Calendar object used for date conversions
     */
    protected Calendar calendar = Calendar.getInstance();

    /**
     * Last modified timestamp's format (result from direct query to MCAT)
     */
    protected static final String DATE_FORMAT = "yyyy-MM-dd-HH.mm.ss";

    abstract protected void setNewFile();
    abstract protected String getDirectoryName(MetaDataRecordList record);
    abstract protected void setDirectoryModDate(MetaDataRecordList record) throws Exception;
    abstract protected SDSCFileObject createNewFileObject(MetaDataRecordList record) throws Exception;
    abstract protected void setFileAttributes(MetaDataRecordList record) throws Exception;
    abstract protected MetaDataRecordList[] doDirectoryQuery(boolean childrenOnly) throws IOException;
    abstract protected MetaDataRecordList[] doFileQuery(boolean userOnly) throws IOException;


    public SDSCFileObject(AbstractFileName fileName, AbstractFileSystem abstractFileSystem) throws FileSystemException
    {
        super(fileName, abstractFileSystem);
        fileAttributes = new HashMap();
        children = new TreeMap();
        this.filePath = UriParser.decode(fileName.getPath());
    }

    @Override
    protected FileType doGetType() throws Exception {
        return (FileType)getAttribute(FILE_TYPE);
    }

    @Override //TODO: consider speeding up this function by not calling doListChildrenResolved() which gathers more details than necessary for this function
    protected String[] doListChildren() throws Exception {
        FileObject[] children = doListChildrenResolved();
        String[] childNames = new String[children.length];
        for(int i=0; i<children.length; i++)
            childNames[i] = children[i].getName().getBaseName();
        return childNames;//return the names of the child files
    }

    /**
     * Detaches this FileObject from its file resource
     * Is Called when this file is closed.
     * Note that this FileObject may be reused later, so should be able to be reattached
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doDetach()
     */
    @Override
    protected void doDetach() {
        // If not overidden, default impl does nothing.
        //nullifying fileInfo or children loses the cache
        this.file = null;
        this.children = null;
    }

    /**
     * Initialises the children map, and invokes functions to get the children objects
     * @throws Exception errors encountered while querying MCAT will be thrown here
     */
    protected void initChildren() throws Exception {
        children = new HashMap();
        getChildrenFiles();
        getChildrenFolders();
    }


    @Override
    protected long doGetContentSize() throws Exception {
        return (Long)this.getAttribute(FILE_SIZE);
    }

    /**
     * Giveen a key, this function with return an attribute from a map, used to cache the details for this object
     * @param s
     * @return items in the file attribute "cache"
     */
    protected Object getAttribute(String s) {
        return fileAttributes.get(s);
    }


    @Override
    protected Map doGetAttributes() throws Exception {
        return fileAttributes;
    }

    @Override
    public void copyFrom(FileObject fileObject, FileSelector fileSelector) throws FileSystemException {
        super.copyFrom(fileObject, fileSelector);

        //this object now needs to re-resolve its details
        try {
            doSetAttribute(PRE_CONFIGURED, null);
            doAttach();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    protected void onChildrenChanged(FileName fileName, FileType fileType) throws Exception {
        super.onChildrenChanged(fileName, fileType);
    }

    @Override
    protected void onChange() throws Exception {
        super.onChange();
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return (Long)this.getAttribute(LAST_MODIFIED);
    }

    @Override
    public void injectType(FileType fileType) {
        super.injectType(fileType);
    }

    @Override
    public void doSetAttribute(String s, Object o) throws Exception {
        fileAttributes.put(s, o);
    }

    @Override
    protected boolean doSetLastModifiedTime(long l) throws Exception {
        return file.setLastModified(l);
    }


    /*
     * Get an SRB Attribute value from a query record
     */
    protected Object getRecordAttribute(MetaDataRecordList record, String key)
    {
        try {
            return record.getValue(record.getFieldIndex(key));
        } catch(Exception e) {
            return "";
        }
    }

    /**
     * This function checks to see if the srb file obejct has been resolved, if it has not, then resolve it, but
     * dont get any extra details for it, the doAttach function will handle this
     *
     * If there is an error trying to resolve this file object then an exception will be thrown
     *
     * @throws Exception
     */
    protected void checkAttached() throws Exception
    {
        if(file == null) {
            setNewFile();
        }
    }

    @Override
    protected void doRename(FileObject fileObject) throws Exception {
        checkAttached(); //check the srb object is resolved
        if (!file.renameTo(((SDSCFileObject)fileObject).file)) {
            throw new FileSystemException("Unable to rename " + file.toString() + " to " + fileObject.toString());
        }
    }


    protected void setDirectoryAttributes(MetaDataRecordList record) throws Exception
    {
        injectType(FileType.FOLDER);
        doSetAttribute(FILE_SIZE, 0);
        doSetAttribute(FILE_TYPE, FileType.FOLDER);
        doSetAttribute(ACCESS_CONSTRAINT, "all");//All folders a readable!
        doSetAttribute(PRE_CONFIGURED, true);
        setDirectoryModDate(record);
    }

    protected void setImaginaryAttributes() throws Exception
    {
        injectType(FileType.IMAGINARY);
        doSetAttribute(FILE_SIZE, 0);
        doSetAttribute(FILE_TYPE, FileType.FILE);
        doSetAttribute(LAST_MODIFIED, 0);
        doSetAttribute(ACCESS_CONSTRAINT, "");
        doSetAttribute(PRE_CONFIGURED, true);
    }

    @Override
    protected void doAttach() throws Exception
    {
        RemoteFile remoteFile = file;
        //if an object resolves itself, ie getChildren() not previously called, then we want to initialise the details of this object
        if(getAttribute(PRE_CONFIGURED) == null) {
            if(remoteFile == null) { //attach the srb file to this object
                setNewFile();
                remoteFile = file;
            }

            if (remoteFile.isDirectory())
            {//if the object is a directory, it does not have a size, set defaults

                //MetaDataRecordList[] results = remoteFile.query(getDirectorySelectQuery());
                MetaDataRecordList[] results = doDirectoryQuery(false);
                if(results != null)
                    setDirectoryAttributes(results[0]);
            }
            else if(remoteFile.exists()) { //must be a file

                MetaDataRecordList[] results = doFileQuery(false);

                if ( results != null )
                    setFileAttributes(results[0]);

                else  //if we cant get details for this file, then we cant show it to anyone, so make it imaginary
                    this.setImaginaryAttributes();
            }
        }
        else {
            //re-attach the filetype to this object, because de-tach changes it to imaginary after vfs reference is lost
            injectType((FileType)getAttribute(FILE_TYPE));
        }
    }

    /**
     * Populates the children map with folder/directory objects only
     * TODO: dogey looking method
     * @throws Exception
     */
    protected void getChildrenFolders() throws Exception {
        GeneralFileSystem fs = fileSystem;
        MetaDataRecordList[] results = doDirectoryQuery(true);

        while ( results != null ) {
            for (int i = 0; i < results.length; i++) {

                String absPath = getDirectoryName(results[i]);
                String dirName = absPath.substring(absPath.lastIndexOf( "/" )+1 );

                //for iRODS, the root directory contains an empty "" directory.
                //VFS cannot result a directory with no name...
                if(!dirName.trim().equals(""))
                {
                    SDSCFileObject fo;

                    if(children.get(dirName) != null)
                        fo = (SDSCFileObject)(children.get(dirName));
                    else
                    {
                        fo = this.createNewFileObject(results[i]);
                        fo.setDirectoryAttributes(results[i]);
                    }
                    //throw the object int the children map
                    children.put(dirName, fo);
                }
            }

            if (!results[results.length-1].isQueryComplete())
                results = results[results.length-1].getMoreResults();
			else
                results = null;
        }
    }


    /**
     * Populates the children map with file objects only
     * @throws Exception
     */
    protected void getChildrenFiles() throws Exception {
        // 2 queries are required: 1 for files, other for folders
        //MetaDataCondition[] conditions = getChildrenFileConditions();

        MetaDataRecordList[] results = doFileQuery(true);
        //MetaDataRecordList[] results = file.query(conditions, this.getFileSelectQuery()); //conditions,
        

        while ( results != null ) {
            for (int i = 0; i < results.length; i++)
            {
                SDSCFileObject fo = createNewFileObject(results[i]);
                fo.setFileAttributes(results[i]);
                children.put(fo.getName().getBaseName(), fo);
            }

            if (!results[results.length-1].isQueryComplete())
                results = results[results.length-1].getMoreResults();
			else
                results = null;
        }

    }

    protected boolean isTrash()
    {
        String[] paths = this.file.getAbsolutePath().split("/");

        boolean isTrash = false;

        //can't be stuffed using regex...
        if(paths.length > 2)
        {
            isTrash = paths[2].equals("trash");
        }

        return isTrash;
    }


}
