package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.HostFileNameParser;

/**
 * @author Jos Koetsier
 * @author David Meredith
 */
public class GridFtpFileNameParser extends HostFileNameParser {

    private final static GridFtpFileNameParser INSTANCE = new GridFtpFileNameParser();


    public GridFtpFileNameParser() {
        super(2811);
    }


    public static FileNameParser getInstance() {
        return INSTANCE;
    }
}