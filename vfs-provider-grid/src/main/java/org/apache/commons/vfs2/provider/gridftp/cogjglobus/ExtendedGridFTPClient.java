package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import java.io.IOException;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.FTPReplyParseException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.globus.ftp.vanilla.Command;

/**
 * @Author Jos Koetsier
 *
 * This class extends the GridFTPClient and adds the 'chmod' command..
 */
public class ExtendedGridFTPClient extends GridFTPClient {

    public ExtendedGridFTPClient(String host, int port) throws IOException, ServerException {
        super(host, port);
    }


    /**
     * Performs the 'chmod' command on a directory or file.
     *
     * @param path Path of the directory or file.
     * @param mode Mode of the file consisting of 3 numbers from 0 to 7.
     * @throws java.io.IOException
     * @throws org.globus.ftp.exception.ServerException
     */
    public void chmod(String path, String mode) throws IOException, ServerException {
        try {
            Command cmd = new Command("SITE CHMOD", mode + " " + path);
            this.controlChannel.execute(cmd);
        } catch (FTPReplyParseException ex) {
            throw new IOException("Parse Error: " + ex.getMessage());
        } catch (UnexpectedReplyCodeException ex) {
            throw new IOException("Unexpected Reply: " + ex.getMessage());
        }

    }
}
