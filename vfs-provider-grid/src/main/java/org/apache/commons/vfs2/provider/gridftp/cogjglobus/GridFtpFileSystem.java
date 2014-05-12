package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import java.io.IOException;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ServerException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * A GridFTP file system for gsiftp uri scheme.
 *
 * @author David Meredith
 * @author Jos Koetsier
 *
 */
public class GridFtpFileSystem extends AbstractFileSystem {

    private static final Log log = LogFactory.getLog(GridFtpFileSystem.class);
    private ExtendedGridFTPClient idleClient = null;
    private Map<String, String> attribs = new HashMap<String, String>();
    //private String homeDirAttribute = "/";
    public static String HOME_DIRECTORY = "HOME_DIRECTORY";
    public static String CREDENTIAL_LIFETIME = "CREDENTIAL_LIFETIME";
    public static String CREDENTIAL = "CREDENTIAL";

    /**
     * Constructor
     *
     * @param rootName
     * @param options
     * @throws org.apache.commons.vfs.FileSystemException if could not connect
     * succesfully to GridFtp server
     */
    protected GridFtpFileSystem(final GenericFileName rootName,
            FileSystemOptions options)
            throws FileSystemException {

        super(rootName, null, options);
        try {
            log.debug("rootName: " + this.getRootName());
            this.createAndConfigureGridFtpClient();
        } catch (Exception ex) {

            String out = "";

            StackTraceElement[] els = ex.getStackTrace();
            for(int i = 0; i < els.length; i++)
            {
                out += els[i].getClassName() + "." + els[i].getMethodName() + "(" + els[i].getFileName() + ":" + els[i].getLineNumber() + ")\n";
            }

            System.out.println(out);
            ex.printStackTrace();
            log.debug("exception in GridFtpFileSystem constructor: \n" + out);
            throw new FileSystemException("vfs.provider.gridftp/connect.error ", rootName);
        }

    }

    private void createAndConfigureGridFtpClient() throws Exception {
        // Create idleClient connection (important, createGridFTPClient
        // does not connect using any given directory path - see below on
        // changingDir to the 'default/target' directory).
        this.idleClient = GridFtpFileSystem.createGridFTPClient((GenericFileName) this.getRootName(), this.getFileSystemOptions());
        String defaultDir = idleClient.getCurrentDir();
        this.attribs.put(GridFtpFileSystem.HOME_DIRECTORY, defaultDir);
        System.out.println(defaultDir);
        //
        idleClient.setPassive();
        idleClient.setLocalActive();
        // return an TYPE_ASCII client by default for file listing operaions,
        // note, client is changed as requried to TYPE_IMAGE for input/ouput
        // and binary stream operations within GridFtpFileObject
        idleClient.setType(Session.TYPE_ASCII);

        // First do a change do the default dir to ensure automount creates
        // users default/home dir.
        try {
            // Need to changeDir to the users default (i.e. home) directory
            // (as returned by getCurrentDir() immediatley after creating
            // initial connection). This is required in order to ensure
            // the default dir always gets created on the server for this
            // ftp session because on some ftp servers, default 'home' or 'target'
            // directories only get created via auto-mount when either a
            // direct connection or explicit 'changeDir()' to this target dir
            // is made !! We need to do this here as the gridftp vfs impl
            // can list parent directories in order to find the
            // relevant fileInfo object (see getInfo() and getChildFile())
            // for the file to be resolved (i.e. on resolveFile()).
            // Thus, the vfs gridftp impl may not initially changeDir to the
            // 'target' directory (rather, its parent) which means auto-mount
            // may not actually create the 'default/target' dir !).
            idleClient.changeDir(defaultDir);
        } catch (Exception ex) {
            log.error(GridFtpFileSystem.class.getName() + " Could not changeDir to default directory", ex);
            throw new FileSystemException("vfs.provider.gridftp/change-work-directory.error", defaultDir);
        }

        // Change to root of fs by default ('/' for gridftp which is *nix)
        // All file operations are relative to the filesystem-root
        String workingDirectory = null;
        if (this.getRootName() != null) {
            workingDirectory = this.getRootName().getPath();
        }
        // userDirIsRoot is null by default.
        // if userDirIsRoot == true, then don't change dir to '/' (note,
        // this Does Not change the root of the fs from '/' to the defaultDir)
        Boolean userDirIsRoot = GridFtpFileSystemConfigBuilder.getInstance().getUserDirIsRoot(this.getFileSystemOptions());
        if (workingDirectory != null && (userDirIsRoot == null || !userDirIsRoot.booleanValue())) {
            try {
                log.debug("changing to root or the given working directory: " + workingDirectory);
                idleClient.changeDir(workingDirectory);
            } catch (Exception ex) {
                log.error(GridFtpFileSystem.class.getName() + " Warning, could not changeDir to root working directory", ex);
                throw new FileSystemException("vfs.provider.gridftp/change-work-directory.error", workingDirectory);
            }
        }


    }

    /**
     * Returns the GridFTPClient of this FileSystem
     *
     * @return the GridFTPClient of this FileSystem
     * @throws org.apache.commons.vfs.FileSystemException if could not return
     *         an established connection to the GridFtp server.
     */
    public ExtendedGridFTPClient getClient() throws FileSystemException {
        synchronized (this) {
            // test to see if client is null
            if (this.idleClient == null || !this.isConnected(idleClient)) {
                try {
                    log.debug("idleClient is null or disconnected, doing reconnect");
                    this.idleClient = null;
                    this.createAndConfigureGridFtpClient();
                } catch (Exception ex) {
                    throw new FileSystemException("vfs.provider.gridftp/connect.error", ex);
                }
            } else {
                // Change dir to the default dir. This is needed incase ftp server
                // uses automount which may have un-mounted this dir due to idle
                // time delays. If this dir is not created, then vfs recurive
                // operations can fail (see createAndConfigureGridFtpClient()).
                try {
                    String defdir = (String) this.attribs.get(GridFtpFileSystem.HOME_DIRECTORY);
                    idleClient.changeDir(defdir);
                } catch (Exception ex) {
                    throw new FileSystemException("vfs.provider.gridftp/connect.error", ex);
                }
            }
            // swap and return client
            ExtendedGridFTPClient returnClient = this.idleClient;
            this.idleClient = null;
            return returnClient;
        }
    }

    private boolean isConnected(GridFTPClient client) {
        if (client == null) {
            return false;
        }
        try {
            String pwd = client.getCurrentDir();
            client.changeDir(pwd);
            return true;
        } catch (IOException ex) {
            log.error("isConnected error", ex);
            //Logger.getLogger( GridFtpFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServerException ex) {
            log.error("isConnected error", ex);
            //Logger.getLogger( GridFtpFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Return the client to the FileSystem for later use.
     *
     * @param putClient
     */
    protected void putClient(ExtendedGridFTPClient putClient) {
        synchronized (this) {
            if (this.idleClient == null) {
                // Hang on to idleClient for later use
                this.idleClient = putClient;
            } else {
                try {
                    // Close the idleClient
                    if (putClient != null) {
                        putClient.close();
                    }
                } catch (Exception ex) {
                    log.error("Failed to close file system cleanly", ex);
                //throw new FileSystemException(ex.getMessage());
                }
            }
        }
    }

    private static ExtendedGridFTPClient createGridFTPClient(GenericFileName fileName,
            FileSystemOptions options) throws /*FileSystemException*/Exception {
        try {
            log.debug("fileName: " + fileName.toString());
            System.out.println(fileName.toString());
            ExtendedGridFTPClient newClient = new ExtendedGridFTPClient(fileName.getHostName(), fileName.getPort());
            GSSCredential credential = GridFtpFileSystemConfigBuilder.getInstance().getGSSCredential(options);
            if (credential == null) {
                throw new FileSystemException("vfs.provider/get-gsscredential.error");
            }
            log.debug("Before:  newClient.authenticate(credential);");
            System.out.println("Before:  newClient.authenticate(credential)");
            newClient.authenticate(credential);
            log.debug("After :  newClient.authenticate(credential);");
            System.out.println("After :  newClient.authenticate(credential)");
            // GSIFTP is predecessor to GridFTP. GSIFTP for early Globus (2001).
            // GridFTP is not fully interoperable with GSIFTP in one respect:
            // in GSIFTP there is no data channel authentication.
            // To successfully communicate to a GSIFTP server,
            // before starting data channel operations you need to inform the
            // GridFTPClient that data channel authentication should not be used:
            // (which is default in GridFTP)
            newClient.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            //
            //newClient.setClientWaitParams(arg0, arg1);
            return newClient;

        } catch (Exception ex) {
            throw ex;
            //throw new FileSystemException("vfs.provider/get-gsscredential.error", ex);
        }
    }

    /**
     * Retrieve information about this filesystem using given attribute names.
     *
     * @param attrName Name of the attribute:
     *  {@link #CREDENTIAL} returns a <code>org.ietf.jgss.GSSCredential</code>
     *  {@link #HOME_DIRECTORY} returns the home directory of the filesystem as a <code>String</code>
     *  {@link #CREDENTIAL_LIFETIME} returns the lifetime in seconds of the credential as an <code>Integer</code>
     * @return the object value related to the given attribute name
     * @throws org.apache.commons.vfs.FileSystemException if an error occurs when
     * fetching requested parameter value.
     */
    @Override
    public Object getAttribute(final String attrName) throws FileSystemException {
        //if (GridFtpFileSystem.HOME_DIRECTORY.equals(attrName)) {
        //    return this.homeDirAttribute;
        //} else
        if (GridFtpFileSystem.CREDENTIAL_LIFETIME.equals(attrName)) {
            try {
                GSSCredential credential = GridFtpFileSystemConfigBuilder.getInstance().getGSSCredential(this.getFileSystemOptions());
                if (credential == null) {
                    throw new FileSystemException("vfs.provider/get-gsscredential.error");
                } else {
                    return credential.getRemainingLifetime();
                }
            } catch (GSSException ex) {
                throw new FileSystemException("vfs.provider/get-gsscredential-lifetime.error");
            }

        } else if (GridFtpFileSystem.CREDENTIAL.equals(attrName)) {
            GSSCredential credential = GridFtpFileSystemConfigBuilder.getInstance().getGSSCredential(this.getFileSystemOptions());
            if (credential == null) {
                throw new FileSystemException("vfs.provider/get-gsscredential.error");
            } else {
                return credential;
            }

        } else {
            //return super.getAttribute(attrName);
            return this.attribs.get(attrName);
        }
    }


    /*public void setAttribute(final String attrName, final String value) throws FileSystemException {
    if (GridFtpFileSystem.HOME_DIRECTORY.equals(attrName)) {
    this.homeDirAttribute = value;
    } else {
    super.setAttribute(attrName, value);
    }
    }*/
    /**
     * Creates a file object. This method is called only if the requested file
     * is not cached.
     *
     * @param name
     * @return a newly created org.apache.commons.vfs.FileObject
     * @throws java.lang.Exception
     */
    protected FileObject createFile(AbstractFileName name) throws Exception {
        //getRootName() Returns the name of the root of this file system
        //System.out.println("rootName: "+getRootName());
        //e.g. getRootName() = gsiftp://ngs.leeds.ac.uk/
        //return new GridFtpFileInfoFileObject(name, this, getRootName());
        return new GridFtpMlsxFileObject(name, this, getRootName());
    }

    /**
     * Adds the capabilities of this file system.
     *
     * @param caps
     */
    protected void addCapabilities(Collection caps) {
        caps.addAll(GridFtpFileProvider.CAPABILITIES);
    }

    /**
     * Close the underlaying link used to access the files
     */
    @Override
    protected synchronized void doCloseCommunicationLink() {
        super.doCloseCommunicationLink();
        try {
            if (this.idleClient != null) {
                this.idleClient.close();
            }
            this.idleClient = null;
        } catch (Exception ex) {
        }
    }
}

