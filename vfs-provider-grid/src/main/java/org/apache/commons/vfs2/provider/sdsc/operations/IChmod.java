package org.apache.commons.vfs2.provider.sdsc.operations;

import org.apache.commons.vfs2.operations.FileOperation;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Jul 23, 2008
 * Time: 2:43:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IChmod extends FileOperation
{
    /**
     * -r   grants/changes access permissions or ownership or ACL  inheritance
            recursively  for data and sub-collections in collection and in all
            sub-collections under it.
     * @param isRecursive
     */
    public void setRecursive(boolean isRecursive);

    /**
     * -i   Set the execution to "ACL inheritance bit" setting mode.
     * @param isInherited
     */
    public void setInheritance(boolean isInherited);

    /**
     * -b   grants/changes access permissions or ownership  in  bulk  for  all
            data  and  sub-collections recursively in collection.  This option
            is same as options  -D -r  given together.
     */
    public void setBulkChange(boolean doBulkChange);

    public void setByCurator(boolean forCurator);

    public void setNewPermission(String perm, String newUserName, String newDomain);
}

