package org.apache.commons.vfs2.provider.storageresourcebroker;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.srb.*;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.sdsc.SDSCFileObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Author: Mathew Wyatt
 * Organisation: James Cook University
 * Date: Jun 5, 2008
 * Time: 11:44:10 AM
 *
 * This is a re-write of the initial bindings provided by Vladimir Silva.
 * This version has massive speed enhancements. Basically by directly querying the MCAT for obejcts and properties
 * in one hit, then caching the infomatoin, and only resolving SRBFile's when they are needed for things 
 * like IOStreams, etc.
 */
public class SRBFileObject extends SDSCFileObject
{

    protected SRBFileExt getSRBObject()
    {
        return (SRBFileExt)(file);
    }

    /**
	 * For items which matched the query, met the conditions above,
	 * the following values will be returned.
	 */
	final static String[] fileSelectFieldNames = {
		SRBMetaDataSet.FILE_NAME,
		SRBMetaDataSet.SIZE,
        SRBMetaDataSet.ACCESS_CONSTRAINT,
        GeneralMetaData.MODIFICATION_DATE,
	};


    final static String[] directorySelectFieldNames = {
        SRBMetaDataSet.PARENT_DIRECTORY_NAME,
        SRBMetaDataSet.DIRECTORY_NAME,
        SRBMetaDataSet.COLL_MODIFY_TIMESTAMP,
	};

    /**
     * Pre-formed selects for the query to get all of the children files from SRB and their associated informatoin
     */
    private final static MetaDataSelect fileSelects[] = MetaDataSet.newSelection(fileSelectFieldNames);
    private final static MetaDataSelect dirSelects[] = MetaDataSet.newSelection(directorySelectFieldNames);
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

    /**
     *
     * @param fileName
     * @param abstractFileSystem
     * @throws FileSystemException
     */
    public SRBFileObject(AbstractFileName fileName, AbstractFileSystem abstractFileSystem) throws FileSystemException {
        super(fileName, abstractFileSystem);
    }

    @Override
    protected boolean doIsReadable() throws Exception {
        String fileAccessConstraint = (String)this.getAttribute(ACCESS_CONSTRAINT);
        if(fileAccessConstraint.equals("read") || fileAccessConstraint.equals("all"))
            return true;
        return false;
    }

    @Override
    protected boolean doIsHidden() throws Exception {
        return false; //todo: fix
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        String fileAccessConstraint = (String)this.getAttribute(ACCESS_CONSTRAINT);
        if(fileAccessConstraint.equals("write") || fileAccessConstraint.equals("all"))
            return true;
        return false;
    }

    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        checkAttached(); //check we have resolved the srb file object
        initChildren(); //fill the children array
        return (SRBFileObject[]) children.values().toArray(new SRBFileObject[children.size()]); //return the kids
    }


    protected SDSCFileObject createNewFileObject(MetaDataRecordList result) throws Exception
    {
        String name	= (String) getRecordAttribute(result, SRBMetaDataSet.FILE_NAME);

        if(name.length() == 0)
        {
            String absPath    = (String)getRecordAttribute(result, SRBMetaDataSet.DIRECTORY_NAME);
            name = absPath.substring(absPath.lastIndexOf( "/" )+1 );
        }
        //create a new FileObject
        SRBFileObject fo = (SRBFileObject) getFileSystem().resolveFile(
                getFileSystem().getFileSystemManager().resolveName(
                        getName(),
                        UriParser.encode(name),
                        NameScope.CHILD));
        return fo;
    }
    
    

    protected void setFileAttributes(MetaDataRecordList record) throws Exception
    {
        final long size		= Long.parseLong((String) getRecordAttribute(record, SRBMetaDataSet.SIZE));
        final String sDate 	= (String) getRecordAttribute(record, GeneralMetaData.MODIFICATION_DATE);//SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP);
        calendar.setTime(DATE_FORMAT.parse(sDate));
        final long lastModifiedL = calendar.getTimeInMillis();
        final String accessConstraint = (String) getRecordAttribute(record, SRBMetaDataSet.ACCESS_CONSTRAINT);

        injectType(FileType.FILE);
        doSetAttribute(FILE_SIZE, size);
        doSetAttribute(FILE_TYPE, FileType.FILE);
        doSetAttribute(LAST_MODIFIED, lastModifiedL);
        doSetAttribute(ACCESS_CONSTRAINT, accessConstraint);
        doSetAttribute(PRE_CONFIGURED, true);
    }

    

    @Override
    protected void doDelete() throws Exception {
        this.checkAttached();
        if (!getSRBFile().delete(isTrash())) {
            throw new FileSystemException("Unable to delete file or folder " + file.toString());
        }
        this.doSetAttribute(FILE_TYPE, FileType.IMAGINARY);
    }



    @Override
    protected void doCreateFolder() throws Exception {
        checkAttached(); //check the srb object is resolved
    	SRBFileExt parent = (SRBFileExt)file.getParentFile();
		String parentFileName = file.getParent();
		if (parent == null || parentFileName.length() == 0)	{	// If parent directory doesn't exist, create all ancestors
			// This is a custom implementation of mkdirs() which works for SRB (see note below). [Rowan McKenzie]
	    	SRBFileExt file = getSRBObject();
			Vector parents = new Vector();
			while (true) {
				if (file == null)
					break;
				parent = (SRBFileExt)file.getParentFile();
				parentFileName = file.getParent();
				if (parent == null || parentFileName.length() == 0)
					break;
				parents.add(parentFileName);
				file = parent;
			}
			for (int i = parents.size()-1; i > 0; i--) {
				new SRBFileExt(file.getFileSystem(), (String)parents.get(i)).mkdir();
			}
		}
        // mkdirs creates all parent folders if necessary. But GeneralFile.mkdirs() doesn't seem to handle
        // SRB paths properly - it creates them relative to the user's home directory. According to 
        // AbstractFileObject, we can assume parents already exist. Is this true? Anyway, we'll use SRBFile.mkdir()
		// here instead because it is SRB aware.
        if (!/*srbFile.mkdirs()*/file.mkdir()) {
            throw new FileSystemException("Could not create SRB directory " + file.toString());
        }
    }
    
    protected void setDirectoryModDate(MetaDataRecordList record) throws Exception
    {
        final String sDate 	= (String) getRecordAttribute(record, SRBMetaDataSet.COLL_MODIFY_TIMESTAMP);
        calendar.setTime(DATE_FORMAT.parse(sDate));
        final long lastModifiedL = calendar.getTimeInMillis();
        doSetAttribute(LAST_MODIFIED, lastModifiedL);    
    }
    

    @Override
    protected InputStream doGetInputStream() throws Exception {
        checkAttached(); //check the srb object is resolved
        return new SRBFileInputStream(getSRBObject());
    }

    @Override
    protected OutputStream doGetOutputStream(boolean b) throws Exception {
        //check the srb object is resolved
        checkAttached();

        //set the srb resource for this object, so srb knows where to write to
        getSRBObject().setResource(((SRBVfsFileSystem)getFileSystem()).getResource(file));

        //if the file does not exist, then we need to create it so we can write to it
        if(!file.exists())
            file.createNewFile();

        return new SRBFileOutputStream(getSRBObject());
    }

    /**
     * Return the actual srb file object
     * @return
     * @throws Exception
     */
    public SRBFileExt getSRBFile() throws Exception {
        checkAttached(); //check the srb object is resolved
        return getSRBObject();
    }

    protected void setNewFile()
    {
        SRBVfsFileSystem fs = (SRBVfsFileSystem)getFileSystem();
        file = new SRBFileExt(fs.getSRBFileSystem(), filePath);
    }

    public boolean isPermInherited() throws FileSystemException
    {
        try
        {
            String[] selectFieldNames = {
                    SRBMetaDataSet.DIRECTORY_LINK_NUMBER,
            };

            MetaDataSelect inheritanceQuery[] = MetaDataSet.newSelection( selectFieldNames );
            MetaDataRecordList[] list = this.getSRBFile().query(inheritanceQuery);

            if(list != null)
            {
                MetaDataRecordList r = list[0];
                String result  = r.getValue(r.getFieldIndex(SRBMetaDataSet.DIRECTORY_LINK_NUMBER)).toString();
                return result.equals("1");
            }

            return false;
        }
        catch(Exception e)
        {
            throw new FileSystemException(e);
        }

    }

    public String getDirectoryName(MetaDataRecordList record)
    {
        return (String) getRecordAttribute(record, SRBMetaDataSet.DIRECTORY_NAME);
    }

    public MetaDataRecordList[] doFileQuery(boolean userOnly) throws IOException
    {
        if(userOnly)
        {
            MetaDataCondition[] conditions = {
                MetaDataSet.newCondition(UserMetaData.USER_NAME,
                        //getUserName
                        MetaDataCondition.EQUAL, ((SRBFileSystem)file.getFileSystem()).getUserName())
            };
            return file.query(conditions, fileSelects);
        }

        return file.query(fileSelects);
    }

    protected MetaDataRecordList[] doDirectoryQuery(boolean childrenOnly) throws IOException
    {
        if(childrenOnly)
        {
            String path = file.getAbsolutePath();

            MetaDataCondition[] conditions = {
                MetaDataSet.newCondition(SRBMetaDataSet.PARENT_DIRECTORY_NAME, MetaDataCondition.EQUAL, path ),
            };

            return ((SRBVfsFileSystem)getFileSystem()).getSRBFileSystem().query(conditions, dirSelects);
        }
        return ((SRBVfsFileSystem)getFileSystem()).getSRBFileSystem().query(dirSelects);
    }


}
