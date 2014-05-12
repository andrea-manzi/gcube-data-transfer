package org.apache.commons.vfs2.provider.irods;

import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.HostFileNameParser;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 4, 2008
 * Time: 1:11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class IRODSFileNameParser extends HostFileNameParser
{
    private final static IRODSFileNameParser INSTANCE = new IRODSFileNameParser();

    public IRODSFileNameParser()
    {
        //default port for srb
        super(1247);
    }

    public static FileNameParser getInstance()
    {
        return INSTANCE;
    }

}
