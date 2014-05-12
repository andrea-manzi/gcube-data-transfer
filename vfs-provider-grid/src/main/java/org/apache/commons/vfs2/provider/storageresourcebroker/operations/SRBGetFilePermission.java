package org.apache.commons.vfs2.provider.storageresourcebroker.operations;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.sdsc.operations.IGetFilePermission;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBFileObject;

import java.util.Map;
import java.util.HashMap;

import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.srb.SRBMetaDataSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Jul 30, 2008
 * Time: 11:01:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class SRBGetFilePermission implements IGetFilePermission
{
    private SRBFileObject srbFile;
    private Map<String, Object> permissionTable;
    private boolean isInherited;

    public SRBGetFilePermission(SRBFileObject file)
    {
        srbFile = file;
    }

    public Map<String, Object>  getPermissionTable()
    {
        return permissionTable;
    }

    public boolean isInherited()
    {
        return isInherited;
    }

    public void process() throws FileSystemException
    {
        permissionTable = new HashMap<String, Object>();
        try
        {
            MetaDataRecordList[] list = srbFile.getSRBFile().getPermissions(true);
            isInherited = srbFile.isPermInherited();

            if(list != null)
            {
                for(int i = 0; i < list.length; i++)
                {
                    MetaDataRecordList r = list[i];
                    String user = (r.getValue(r.getFieldIndex(SRBMetaDataSet.USER_NAME))).toString();
                    String domain = (r.getValue(r.getFieldIndex(SRBMetaDataSet.USER_DOMAIN))).toString();
                    String constraint = "";
                    if(srbFile.getSRBFile().isDirectory())
                    {
                        constraint  = r.getValue(r.getFieldIndex(SRBMetaDataSet.DIRECTORY_ACCESS_CONSTRAINT)).toString();
                    }
                    else
                    {
                        constraint  = r.getValue(r.getFieldIndex(SRBMetaDataSet.ACCESS_CONSTRAINT)).toString();
                    }

                    permissionTable.put(user + "@" + domain, constraint);
                }
            }
        }
        catch(Exception e)
        {
            throw new FileSystemException("Unable to get file permission " + e.toString());
        }

    }
}
