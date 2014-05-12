package org.apache.commons.vfs2.provider.irods;

import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.sdsc.SDSCVfsFileSystem;
import org.apache.commons.vfs2.*;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.io.IOException;

import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFileSystem;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 4, 2008
 * Time: 10:43:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class IRODSVfsFileSystem extends SDSCVfsFileSystem
{
    private Logger logger = Logger.getLogger(IRODSVfsFileSystem.class);
    protected IRODSFileSystem irodsFileSystem;


    public IRODSVfsFileSystem(final FileName rootName, final FileSystemOptions opts)
    	throws FileSystemException
    {
        super(rootName, opts);
        
        try
        {
            IRODSFileSystemConfigBuilder configurations = IRODSFileSystemConfigBuilder.getInstance();
            IRODSAccount account = configurations.getAccount(opts);
            irodsFileSystem = new IRODSFileSystem(account);
            attribs.put(ACCOUNT, account);
            attribs.put(FILESYSTEM, irodsFileSystem);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new FileSystemException(e.getMessage(), e);
		}
        
    }

    protected FileObject createFile(final AbstractFileName name) throws FileSystemException
    {
        IRODSFileObject fo = null;
        fo = new IRODSFileObject(name, this);
        return fo;
    }



    protected void doCloseCommunicationLink()
    {
        try
        {
            irodsFileSystem.close();
        }
        catch(IOException e)
        {
            logger.error("Error closing iRODS connection", e);
        }
    }

    /**
     * Returns the jargon SRBFilesystem for the connection
     * @return
     */
    protected IRODSFileSystem getIRODSFileSystem() {
        return irodsFileSystem;
    }

    public String getDefaultStorageResource()
    {
        return ((IRODSAccount)irodsFileSystem.getAccount()).getDefaultStorageResource();
    }


	@Override
	protected void addCapabilities(Collection<Capability> caps) {
		caps.addAll(DefaultLocalFileProvider.capabilities);
		
	}
}
