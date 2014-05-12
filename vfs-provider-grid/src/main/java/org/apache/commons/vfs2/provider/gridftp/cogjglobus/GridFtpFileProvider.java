package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;

/**
 * Ths VFS provider class for Grid FTP
 *
 * @author Jos Koetsier
 * @author David Meredith
 *
 */
public class GridFtpFileProvider extends AbstractOriginatingFileProvider {

    protected static final Collection CAPABILITIES = Collections.unmodifiableCollection(
            Arrays.asList(new Capability[]{
        Capability.RENAME,
        Capability.DELETE,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.APPEND_CONTENT,
        Capability.CREATE,
        Capability.GET_LAST_MODIFIED,
    // Capability.SET_LAST_MODIFIED_FILE,
    }));

    /**
     * default constructor
     */
    public GridFtpFileProvider() {
        super();
        setFileNameParser(org.apache.commons.vfs2.provider.gridftp.cogjglobus.GridFtpFileNameParser.getInstance());
    }

    /**
     * Creates a {@link org.apache.commons.vfs.FileSystem}.
     * If the returned FileSystem implements {@link
     * org.apache.commons.vfs.provider.VfsComponent}, it will be initialised.
     *
     * @param rootName The name of the root file of the file system to create.
     * @param fileSystemOptions
     * @return the created file system
     * @throws org.apache.commons.vfs.FileSystemException
     *
     */
    protected FileSystem doCreateFileSystem(FileName rootName,
            FileSystemOptions fileSystemOptions)
            throws FileSystemException {
    	System.out.println(rootName);
        /*
         * It is CRITICAL when this method is called because it creates a new
         * GridFtpFileSystem, which itself contains the gridftp client connection
         * for each user. It is called on FileSystemManager.resolveFile(uri)
         * when the file doesnt exist in cache (e.g. when a NEW server is given in the uri)
         */
        return new GridFtpFileSystem((GenericFileName) rootName, fileSystemOptions);
    }

    /**
     * Get the file systems capabilities
     *
     * @return {@link org.apache.vfs.Capability} collection
     */
    public Collection getCapabilities() {
        return CAPABILITIES;
    }
}
