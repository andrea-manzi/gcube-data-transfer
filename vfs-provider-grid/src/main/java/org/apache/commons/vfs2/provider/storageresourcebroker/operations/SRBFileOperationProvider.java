package org.apache.commons.vfs2.provider.storageresourcebroker.operations;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.operations.AbstractFileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperation;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBFileObject;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Jul 23, 2008
 * Time: 2:43:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class SRBFileOperationProvider extends AbstractFileOperationProvider
{

    public SRBFileOperationProvider()
    {
        try
        {
            this.addOperation(SRBSchmod.class);
            this.addOperation(SRBGetFilePermission.class);
        }
        catch(FileSystemException e)
        {
            //this shouldn't happen at all
        }
    }

    protected void doCollectOperations(Collection availOps, Collection resultList, FileObject file)
    {
       //NOT TOO SURE WHAT THIS METHOD IS SUPPOSE TO DO
    }

    protected FileOperation instantiateOperation(FileObject file, Class opClass) throws FileSystemException
    {
        if(!(file instanceof SRBFileObject))
        {
            throw new FileSystemException("vfs.operation/wrong-type.error", opClass);
        }

        SRBFileObject srbFile = (SRBFileObject)(file);

        if(opClass == SRBSchmod.class)
        {
            return new SRBSchmod(srbFile);
        }
        else if(opClass == SRBGetFilePermission.class)
        {
            return new SRBGetFilePermission(srbFile);
        }

        throw new FileSystemException("vfs.operation/wrong-type.error", opClass);
    }

}

