package org.apache.commons.vfs2.provider.irods;

import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sdsc.SDSCSystemConfigBuilder;

import edu.sdsc.grid.io.irods.IRODSFileSystem;
import edu.sdsc.grid.io.irods.IRODSAccount;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 4, 2008
 * Time: 10:28:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class IRODSFileSystemConfigBuilder extends SDSCSystemConfigBuilder
{
    private final static IRODSFileSystemConfigBuilder builder = new IRODSFileSystemConfigBuilder();

    public void setAccount(IRODSAccount account, FileSystemOptions opts) {
        setParam(opts, ACCOUNT, account);
    }

    public static IRODSFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    public IRODSAccount getAccount(FileSystemOptions opts) {
        return (IRODSAccount) getParam(opts, ACCOUNT);
    }
    
    protected Class getConfigClass()
    {
        return IRODSFileSystem.class;
    }
}
