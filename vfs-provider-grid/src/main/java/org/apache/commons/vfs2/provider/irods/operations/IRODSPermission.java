package org.apache.commons.vfs2.provider.irods.operations;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 11, 2008
 * Time: 10:19:06 AM
 * To change this template use File | Settings | File Templates.
 */
public enum IRODSPermission 
{
    none, read, write, own;

    public String toString()
    {
        if(this.name().equals("none"))
            return "null";
        return this.name();
    }
}
