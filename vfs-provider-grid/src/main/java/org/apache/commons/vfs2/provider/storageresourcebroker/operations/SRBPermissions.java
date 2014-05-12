package org.apache.commons.vfs2.provider.storageresourcebroker.operations;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Jul 30, 2008
 * Time: 11:00:52 AM
 * To change this template use File | Settings | File Templates.
 */

public enum SRBPermissions 
{
    write, read, annotate, none, curate, ownership, all;

    public String toString()
    {
        if(this.name().equals("none"))
            return "null";
        return this.name();
    }
}
