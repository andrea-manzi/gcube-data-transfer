package org.apache.commons.vfs2.provider.irods;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.sdsc.SDSCFileObject;

import edu.sdsc.grid.io.irods.*;
import edu.sdsc.grid.io.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 4, 2008
 * Time: 11:24:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class IRODSFileObject extends SDSCFileObject
{
    //These are hidden inside the packages :/  So I have to copy them out here.
    public static final String FILE_ACCESS_TYPE = "File Access Type";

    /**
	 * For items which matched the query, met the conditions above,
	 * the following values will be returned.
	 */
	final static String[] fileSelectFieldNames = {
		GeneralMetaData.FILE_NAME,
            IRODSMetaDataSet.SIZE,
            FILE_ACCESS_TYPE,
            GeneralMetaData.MODIFICATION_DATE,
	};


    final static String[] directorySelectFieldNames = {
        GeneralMetaData.DIRECTORY_NAME,
        IRODSMetaDataSet.PARENT_DIRECTORY_NAME,
        IRODSMetaDataSet.DIRECTORY_NAME,
        IRODSMetaDataSet.DIRECTORY_MODIFY_DATE,
	};

    private final static MetaDataSelect fileSelects[] = MetaDataSet.newSelection(fileSelectFieldNames);
    private final static MetaDataSelect dirSelects[] = MetaDataSet.newSelection(directorySelectFieldNames);


    protected IRODSFile getIRODSFile()
    {
        return (IRODSFile)file;
    }

    protected IRODSFileObject(AbstractFileName name, IRODSVfsFileSystem system) throws FileSystemException
    {
        super(name, system);
    }

        /**
     * Return the actual srb file object
     * @return
     * @throws Exception
     */
    public IRODSFile getIRODSFIle() throws Exception {
        checkAttached(); //check the srb object is resolved
        return getIRODSFile();
    }

    protected void setNewFile()
    {
        IRODSVfsFileSystem fs = (IRODSVfsFileSystem)this.getFileSystem();
        file = new IRODSFile(fs.getIRODSFileSystem(), filePath);
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        checkAttached(); //check the srb object is resolved
        return new IRODSFileInputStream(getIRODSFile());
    }

    @Override
    protected OutputStream doGetOutputStream(boolean b) throws Exception {
        //check the srb object is resolved
        checkAttached();

        //set the srb resource for this object, so srb knows where to write to
        getIRODSFile().setResource(((IRODSVfsFileSystem)getFileSystem()).getResource(file));

        //if the file does not exist, then we need to create it so we can write to it
        if(!file.exists())
            file.createNewFile();

        return new IRODSFileOutputStream(getIRODSFile());
    }

    protected void setDirectoryAttributes(MetaDataRecordList record) throws Exception
    {
        super.setDirectoryAttributes(record);
        doSetAttribute(ACCESS_CONSTRAINT, 1120);//All folders a readable!
    }

    protected String getDirNameKey()
    {
        return IRODSMetaDataSet.DIRECTORY_NAME;
    }

    protected void setDirectoryModDate(MetaDataRecordList record) throws Exception
    {
        final String sDate 	= (String) getRecordAttribute(record, IRODSMetaDataSet.DIRECTORY_MODIFY_DATE);
        final long lastModifiedL = Long.parseLong(sDate);
        doSetAttribute(LAST_MODIFIED, lastModifiedL);
    }

    protected SDSCFileObject createNewFileObject(MetaDataRecordList record) throws Exception
    {
        String name	= (String) getRecordAttribute(record, IRODSMetaDataSet.FILE_NAME);

        if(name.length() == 0)
        {
            String absPath = (String)getRecordAttribute(record, IRODSMetaDataSet.DIRECTORY_NAME);
            name = absPath.substring(absPath.lastIndexOf( "/" )+1 );
        }
        
        //create a new FileObject
        IRODSFileObject fo = (IRODSFileObject) getFileSystem().resolveFile(
                getFileSystem().getFileSystemManager().resolveName(
                        getName(),
                        UriParser.encode(name),
                        NameScope.CHILD));
        return fo;
    }

    protected void setFileAttributes(MetaDataRecordList record) throws Exception
    {
        final long size		= Long.parseLong((String) getRecordAttribute(record, IRODSMetaDataSet.SIZE));
        final String sDate 	= (String) getRecordAttribute(record, GeneralMetaData.MODIFICATION_DATE);
        final long lastModifiedL = Long.parseLong(sDate);
        final Integer accessConstraint = Integer.parseInt((String)(getRecordAttribute(record, FILE_ACCESS_TYPE)));
        injectType(FileType.FILE);
        doSetAttribute(FILE_SIZE, size);
        doSetAttribute(FILE_TYPE, FileType.FILE);
        doSetAttribute(LAST_MODIFIED, lastModifiedL);
        doSetAttribute(ACCESS_CONSTRAINT, accessConstraint);
        doSetAttribute(PRE_CONFIGURED, true);
    }

    @Override
    protected boolean doIsReadable() throws Exception {
        Integer fileAccessConstraint = (Integer)this.getAttribute(ACCESS_CONSTRAINT);
        
        return (fileAccessConstraint >= 1050);
    }

    @Override
    protected boolean doIsHidden() throws Exception {
        return false; //todo: fix
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        Integer fileAccessConstraint = (Integer)this.getAttribute(ACCESS_CONSTRAINT);
        return (fileAccessConstraint >= 1120);
    }

    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        checkAttached(); //check we have resolved the srb file object
        initChildren(); //fill the children array
        return (IRODSFileObject[]) children.values().toArray(new IRODSFileObject[children.size()]); //return the kids
    }


    @Override
    protected void doCreateFolder() throws Exception {
        checkAttached(); //check the srb object is resolved
    	IRODSFile parent = (IRODSFile)(getIRODSFile().getParentFile());
		String parentFileName = file.getParent();

        if (parent == null || parentFileName.length() == 0)	{	// If parent directory doesn't exist, create all ancestors
			// This is a custom implementation of mkdirs() which works for SRB (see note below). [Rowan McKenzie]


	    	IRODSFile file = getIRODSFile();
			Vector parents = new Vector();
			while (true) {
				if (file == null)
					break;
				parent = (IRODSFile)file.getParentFile();
				parentFileName = file.getParent();

				if (parent == null || parentFileName.length() == 0)
					break;


				parents.add(parentFileName);
				file = parent;
			}
			for (int i = parents.size()-1; i > 0; i--) {
				new IRODSFile((IRODSFileSystem)file.getFileSystem(), (String)parents.get(i)).mkdir();
			}
		}
        // mkdirs creates all parent folders if necessary. But GeneralFile.mkdirs() doesn't seem to handle
        // SRB paths properly - it creates them relative to the user's home directory. According to
        // AbstractFileObject, we can assume parents already exist. Is this true? Anyway, we'll use SRBFile.mkdir()
		// here instead because it is SRB aware.

        if (!/*srbFile.mkdirs()*/file.mkdir()) {
            throw new FileSystemException("Could not create iRODS directory " + file.toString());
        }
       
    }

    @Override
    protected void doDelete() throws Exception {
        this.checkAttached();
        
        if (!file.delete()) {
            throw new FileSystemException("Unable to delete file or folder " + file.toString());
        }
        this.doSetAttribute(FILE_TYPE, FileType.IMAGINARY);
    }

    public String getDirectoryName(MetaDataRecordList record)
    {
        return (String) getRecordAttribute(record, IRODSMetaDataSet.DIRECTORY_NAME);
    }

    protected MetaDataRecordList[] doDirectoryQuery(boolean childrenOnly) throws IOException
    {
        if(childrenOnly)
        {
            String path = file.getAbsolutePath();

            MetaDataCondition[] conditions = {
                MetaDataSet.newCondition(IRODSMetaDataSet.PARENT_DIRECTORY_NAME, MetaDataCondition.EQUAL, path ),
            };

            return ((IRODSVfsFileSystem)getFileSystem()).getIRODSFileSystem().query(conditions, dirSelects);
        }
        return ((IRODSVfsFileSystem)getFileSystem()).getIRODSFileSystem().query(dirSelects);


    }

    protected MetaDataRecordList[] doFileQuery(boolean userOnly) throws IOException
    {
        if(userOnly)
        {
            MetaDataCondition[] conditions = {
                MetaDataSet.newCondition(UserMetaData.USER_NAME,
                        //getUserName
                        MetaDataCondition.EQUAL, ((IRODSFileSystem)file.getFileSystem()).getUserName())
            };
            return file.query(conditions, fileSelects);
        }
        return file.query(fileSelects);
    }
}
