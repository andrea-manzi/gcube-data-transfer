package org.apache.commons.vfs2.provider.storageresourcebroker;

import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.HostFileNameParser;

/**
 * Author: Mathew Wyatt
 * Organisation: James Cook University
 * Date: Jun 5, 2008
 * Time: 11:37:52 AM
 */
public class SRBFileNameParser extends HostFileNameParser
{
    private final static SRBFileNameParser INSTANCE = new SRBFileNameParser();

    public SRBFileNameParser()
    {
        //default port for srb
        super(5544);
    }

    public static FileNameParser getInstance()
    {
        return INSTANCE;
    }
}
