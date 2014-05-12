package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.gridftp.cogjglobus.GridFtpFileSystem;
import org.ietf.jgss.GSSCredential;

/**
 * GridFtpFileSystem configurer
 *
 * @author David Meredith
 */
public class GridFtpFileSystemConfigBuilder extends FileSystemConfigBuilder {

    private static GridFtpFileSystemConfigBuilder singleton;
    private final static String USER_DIR_IS_ROOT = GridFtpFileSystemConfigBuilder.class.getName() + ".USER_DIR_IS_ROOT";
    private final static String GSSCREDENTIAL = GridFtpFileSystemConfigBuilder.class.getName() + ".GSSCREDENTIAL";
    //private final static String TIMEOUT = GridFtpFileSystemConfigBuilder.class.getName() + ".TIMEOUT";

    private GridFtpFileSystemConfigBuilder() {
    }


    /**
     * Get the static singleton <code>GridFtpFileSystemConfigBuilder</code>
     *
     * @return static <code>GridFtpFileSystemConfigBuilder</code>
     */
    public synchronized static GridFtpFileSystemConfigBuilder getInstance() {
        if (singleton == null) {
            singleton = new GridFtpFileSystemConfigBuilder();
        }
        return singleton;
    }


    protected Class getConfigClass() {
        return GridFtpFileSystem.class;
    }


    /**
     * Set the GSSCrednetial to use for authentication. If not set, then
     * the default user credential will be used in <code>$HOME/.globus/cog.properties</code>
     *
     * @param options add the credential to these options
     * @param credential
     */
    public void setGSSCredential(FileSystemOptions options,
        GSSCredential credential) {
        setParam(options, GSSCREDENTIAL, credential);
    }


    /**
     * Get the GSSCredential from the given <code>FileSystemOptions</code>
     *
     * @param options
     * @return globus credential
     */
    public GSSCredential getGSSCredential(FileSystemOptions options) {
        return (GSSCredential) getParam(options, GSSCREDENTIAL);
    }

    /**
     * If true, use the users home/default directory as root (do not change to the
     * file systems root, e.g. '/').
     * Set to true to use the users home/default dir as root.
     * Set to false to use '/' as the users home/default dir (default)
     *
     * @param opts
     * @param userDirIsRoot
     */
    public void setUserDirIsRoot(FileSystemOptions opts, boolean userDirIsRoot) {
        // TODO - include dave ?
        setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @param opts
     * @return true if have set
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(FileSystemOptions opts) {
        return (Boolean) getParam(opts, USER_DIR_IS_ROOT);
    }
}

