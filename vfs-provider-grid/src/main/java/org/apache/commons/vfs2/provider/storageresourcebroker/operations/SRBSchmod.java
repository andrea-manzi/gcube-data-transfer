package org.apache.commons.vfs2.provider.storageresourcebroker.operations;

import org.apache.commons.vfs2.provider.sdsc.operations.IChmod;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBFileObject;
import org.apache.commons.vfs2.FileSystemException;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileExt;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Jul 23, 2008
 * Time: 2:44:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SRBSchmod implements IChmod
{
    private boolean isRecurssive;
    private boolean isInherited;
    private boolean isBulkChange;
    private SRBFileObject file;
    private String newUserName;
    private String newUserDomain;
    private String perm;
    private boolean byCurator;


    public SRBSchmod(SRBFileObject _file)
    {
        file = _file;
        isRecurssive = false;
        isInherited = false;
        isBulkChange = false;
        newUserName = null;
        newUserDomain = null;
        byCurator = false;
    }

    public void setRecursive(boolean _isRecurssive)
    {
        isRecurssive = _isRecurssive;
    }

    public void setInheritance(boolean _isInherited)
    {
        isInherited = _isInherited;
    }

    public void setBulkChange(boolean _isBulkChange)
    {
        isBulkChange = _isBulkChange;
    }

    public void setByCurator(boolean _byCurator)
    {
        byCurator = _byCurator;
    }

    public void setNewPermission(String _perm, String _newUserName, String _newUserDomain)
    {
        perm = _perm;
        newUserName = _newUserName;
        newUserDomain = _newUserDomain;
    }


    public void process() throws FileSystemException
    {
        try
        {
            if((newUserName == null) || (newUserDomain == null))
            {
                throw new FileSystemException("Cannot change permission without specifying user and user domain");
            }

            SRBFileExt srbFile = file.getSRBFile();
            srbFile.setStickyBit(false, isInherited, isRecurssive);

            srbFile.changePermissions(perm, newUserName, newUserDomain, isRecurssive);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new FileSystemException("Cannot change permssion " + e.toString());

        }
    }
}
