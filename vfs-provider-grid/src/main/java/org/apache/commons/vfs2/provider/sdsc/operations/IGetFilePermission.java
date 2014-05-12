package org.apache.commons.vfs2.provider.sdsc.operations;

import org.apache.commons.vfs2.operations.FileOperation;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Jul 30, 2008
 * Time: 10:57:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IGetFilePermission extends FileOperation
{
    /**
     * A Map of keys (user@domain) and a string of the permission
     * type (i.e. read, write, etc) is retruned.  If the permission
     * is sticky, a (I) is appended to the permssion string.
     * @return
     */
    public Map<String, Object>  getPermissionTable();

    public boolean isInherited();
}
