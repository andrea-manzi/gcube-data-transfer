package org.apache.commons.vfs2.provider.storageresourcebroker;

import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFileSystemExt;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.sdsc.SDSCVfsFileSystem;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import java.io.IOException;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Author: Mathew Wyatt
 * Organisation: James Cook University
 * Date: Jun 5, 2008
 * Time: 11:44:28 AM
 */
public class SRBVfsFileSystem extends SDSCVfsFileSystem {
     private static Log log = LogFactory.getLog(SRBVfsFileSystem.class);
    /**
     * Holds the SRBFileSystem which is the root of the connection to the SRB server
     */
    private SRBFileSystemExt srbFileSystem;

    public static final String MCAT_ZONE = "MCAT_ZONE";
    public static final String MDAS_DOMAIN = "MDAS_DOMAIN";
    public static final String DEFAULT_RESOURCE = "DEFAULT_RESOURCE";
    public static String CREDENTIAL_LIFETIME = "CREDENTIAL_LIFETIME";
    public static String CREDENTIAL = "CREDENTIAL";

    /**
     * Creates a conection to an SRB filesystem via a given SRBAccount
     * @param rootName
     * @param fileSystemOptions
     * @throws FileSystemException
     */
    protected SRBVfsFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        super(rootName, fileSystemOptions);

        try {
            SRBFileSystemConfigBuilder configurations = SRBFileSystemConfigBuilder.getInstance();
            //SRBAccount would have been created at this point in time.  It's stored in
            //the FileSystemOptions key ACCOUNT.
            SRBAccount srbAccount = configurations.getAccount(fileSystemOptions);
            srbFileSystem = new SRBFileSystemExt(srbAccount);
            attribs.put(ACCOUNT, srbFileSystem.getAccount());
            attribs.put(FILESYSTEM, srbFileSystem);
            attribs.put(HOME_DIRECTORY, srbAccount.getHomeDirectory());
            attribs.put(DEFAULT_RESOURCE, srbAccount.getDefaultStorageResource());
            attribs.put(MCAT_ZONE, srbAccount.getMcatZone());
            attribs.put(MDAS_DOMAIN, srbAccount.getDomainName());
        } catch (Throwable e) {
            throw new FileSystemException(e);
		}
    }


    /**
     * Close the connection to the SRB FilsSystem
     */
    protected void doCloseCommunicationLink() {
        try {
            srbFileSystem.close();
        } catch (IOException e) {
            //TODO: somthing here
        }
    }

    /**
     * Returns the jargon SRBFilesystem for the connection
     * @return
     */
    protected SRBFileSystemExt getSRBFileSystem() {
        return srbFileSystem;
    }
 
    public String getDefaultStorageResource()
    {
        return ((SRBAccount)srbFileSystem.getAccount()).getDefaultStorageResource();
    }


    /**
     * Copying a whole bunch of methods from David Meredith's SRB Implementation (see djm branch)
     */

    /**
     * Creates a file object.
     */
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException
    {
        return new SRBFileObject(name, this);
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.addAll(SRBFileProvider.capabilities);
    }


    /**
     * gets the SRBFileSystem of this SrbFileSystem
     * @return
     * @throws org.apache.commons.vfs.FileSystemException
     */
    public SRBFileSystemExt getClient() throws FileSystemException {
        // ensure the idleClient is active, if not, then reconnect/recreate
        synchronized (this) {
            try {
                if (this.srbFileSystem == null || this.srbFileSystem.isClosed()) {
                    log.debug("idleClient is null, doing reconnect");
                    this.srbFileSystem = this.createSrbFileSystem((GenericFileName) this.getRootName(), this.getFileSystemOptions());
                }
            } catch (Exception ex) {
                throw new FileSystemException("vfs.provider.srb/connect.error", ex);
            }
            SRBFileSystemExt returnClient = this.srbFileSystem;
            this.srbFileSystem = null;
            return returnClient;
        }
    }

    /**
     * This only gets called if the filesystem hasn't been created or it's closed
     * @param rootName
     * @param opts
     * @return
     * @throws FileSystemException
     */
    private SRBFileSystemExt createSrbFileSystem(GenericFileName rootName,
        FileSystemOptions opts) throws FileSystemException {
        try {
            log.debug("before srbAccount creation ");
            SRBAccount srbAccount = SRBVfsFileSystem.createSrbAccount(rootName, opts);
            SRBFileSystemExt _srbFileSystem = new SRBFileSystemExt(srbAccount);
            log.debug("after srbAccount creation");
            // update the firewall port range
            Integer portMin = SRBFileSystemConfigBuilder.getInstance().getFileWallPortMin(opts);
            Integer portMax = SRBFileSystemConfigBuilder.getInstance().getFileWallPortMax(opts);
            if (portMin != null && portMax != null) {
                _srbFileSystem.setFirewallPorts(portMin, portMax); // 64000, 65000
            }

            return _srbFileSystem;

        } catch (Exception ex) {
            throw new FileSystemException("vfs.provider.srb/connect.error", rootName, ex);
        }
    }

    /**
     * @param attrName either, HOME_DIRECTORY, DEFAULT_RESOURCE,
     * MCAT_ZONE, MDAS_DOMAIN, CREDENTIAL_LIFETIME, CREDENTIAL
     * @return depends on the argument (all strings).
     * @throws org.apache.commons.vfs.FileSystemException
     */
    @Override
    public Object getAttribute(final String attrName) throws FileSystemException {
        if (SRBVfsFileSystem.CREDENTIAL_LIFETIME.equals(attrName)) {
            try {
                GSSCredential credential = SRBFileSystemConfigBuilder.getInstance().getGSSCredential(this.getFileSystemOptions());
                if (credential == null) {
                    throw new FileSystemException("vfs.provider/get-gsscredential.error");
                } else {
                    return credential.getRemainingLifetime();
                }
            } catch (GSSException ex) {
                throw new FileSystemException("vfs.provider/get-gsscredential-lifetime.error", ex);
            }
        } else if (SRBVfsFileSystem.CREDENTIAL.equals(attrName)) {
            GSSCredential credential = SRBFileSystemConfigBuilder.getInstance().getGSSCredential(this.getFileSystemOptions());
            if (credential == null) {
                throw new FileSystemException("vfs.provider/get-gsscredential.error");
            } else {
                return credential;
            }

        } else {
            return attribs.get(attrName);
        }
    }

    /**
     * SRBAccount does not actually connect to a filesystem.
     * It only hold user connection information.
     * <p>
     *
     * SRB Connection Info Requirements
     * ==================================
     * In order to connect to srb, following data is needed:
     *
     * + Host:Port
     * + Port range (portMin, portMax)
     *
     * if(GSI)
     *      + GSSCredential
     *      +/- defaultStorageResource (should this be +)
     *      +/- homeDirectory
     *
     *
     * if(ENCRYPT1)
     *      + username
     *      + password
     *      + homeDirectory
     *      + mdasDomainName
     *      + defaultStorageResource
     *      +/- mcatZone
     *
     *  Authentication token order of precidence:
     *  =========================================
     *  1) username and password given in URI
     *  2) username and password given in static authenticator (not in uri)
     *  3) proxy certificate given in SrbFileSystemConfigBuilder
     *  4) auth tokens read from local .MdasEnv .MdasAuth
     * <p>
     *
     * @param rootName
     * @param opts
     * @return
     * @throws org.apache.commons.vfs.FileSystemException
     */
    private static SRBAccount createSrbAccount(GenericFileName rootName,
        FileSystemOptions opts) throws FileSystemException {

        // If passed the SRBAccount via the SRBFileSystemConfigBuilder, then
        // use this account which takes precidence.
        if(SRBFileSystemConfigBuilder.getInstance().getAccount(opts) != null){
            return SRBFileSystemConfigBuilder.getInstance().getAccount(opts);
        }

        // Info used to make an srb connection (maybe incomplete due to
        // diferent ways used to make srb connection, todo: check all ways).
        String host = rootName.getHostName();
        Integer port = rootName.getPort();
        String mdasUserInfoDirectory = SRBFileSystemConfigBuilder.getInstance().getMdasUserInfoDirectory(opts);
        String defaultStorageResource = SRBFileSystemConfigBuilder.getInstance().getDefaultStorageResource(opts);
        GSSCredential credential = SRBFileSystemConfigBuilder.getInstance().getGSSCredential(opts);
        String homeDir = SRBFileSystemConfigBuilder.getInstance().getHomeDirectory(opts);
        String mdasDomainName = SRBFileSystemConfigBuilder.getInstance().getMdasDomainName(opts);
        String mcatZone = SRBFileSystemConfigBuilder.getInstance().getMcatZone(opts);
        boolean useLocalMdasFiles = SRBFileSystemConfigBuilder.getInstance().getUseLocalMdasFiles(opts);
        String username = null;
        String password = null;

        //log.debug(mdasUserInfoDirectory + " " + defaultStorageResource + " " + homeDir + " " + mdasDomainName + " " + mcatZone + " " + useLocalMdasFiles);

        // Determine the username and password to use (given in either URI, UserAuthenticationData, or not given).
        UserAuthenticationData authData = null;
        try {
            authData = UserAuthenticatorUtils.authenticate(opts, SRBFileProvider.AUTHENTICATOR_TYPES);
            if (authData != null) {
                //gets data of given type from the UserAuthenticationData or
                //null if there is no data or data of this type is not available
                //(AuthenticationData authData, UserAuthenticationData.Type type, char[] overwriddenValue)
                // if the usernameCharArray and passwordCharArray is given in the uri, then that overides
                // the usernameCharArray and passwordCharArray given in the static username authenticator.
                // (note vfs docs misleading here, it should state 'overiddingValue' rather than 'overiddenValue').
                char[] usernameCharArray = UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getUserName()));
                char[] passwordCharArray = UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getPassword()));
                if (usernameCharArray != null)
                    username = UserAuthenticatorUtils.toString(usernameCharArray);
                if (passwordCharArray != null)
                    password = UserAuthenticatorUtils.toString(passwordCharArray);
            }
        } finally {
            if (authData != null)
                UserAuthenticatorUtils.cleanup(authData);
        }
        /*if (usernameCharArray == null)
        usernameCharArray = "anonymous".toCharArray();
        if (passwordCharArray == null)
        passwordCharArray = "anonymous".toCharArray();*/

        boolean isENCRYPT1 = false;
        boolean isGSI = false;
        if (username != null && password != null) {
            isENCRYPT1 = true;
        } else if (credential != null && !isENCRYPT1) {
            isGSI = true;
        }

        SRBAccount srb = null;
        try {
            if (isENCRYPT1) {
                log.debug("Create SRBAccount using ENCRYPT1");
                if (homeDir != null && mdasDomainName != null &&
                    defaultStorageResource != null && mcatZone != null) {
                    //
                    srb = new SRBAccount(host, port, username, password,
                        homeDir, mdasDomainName, defaultStorageResource, mcatZone);

                } else if (homeDir != null && mdasDomainName != null &&
                    defaultStorageResource != null) {
                    //
                    srb = new SRBAccount(host, port, username, password,
                        homeDir, mdasDomainName, defaultStorageResource);
                }

            } else if (isGSI) {
                if (homeDir != null && defaultStorageResource != null) {
                    log.debug("Create SRBAccount using GSI with homeDir and storageResource");
                    srb = new SRBAccount(host, port, credential, homeDir,
                        defaultStorageResource, SRBAccount.GSI_AUTH);
                } else {
                    // uses special constructor that sets GSI_AUTH and
                    // implements a login procedure using the 'ticketuser' in
                    // order to get the users home directory
                    log.debug("Create SRBAccount using GSI ticket user");
                    srb = new SRBAccount(host, port, credential);
                }

            }
            // fall back to using local MdasFiles (true by default).
            if (srb == null && useLocalMdasFiles) {
                log.debug("Create SRBAccount using local MDAS files");
                // Load some stuff from ~/.srb/.MdasEnv and .MdasAuth files
                // (use default location unless otherwise specified).
                if (mdasUserInfoDirectory == null) {
                    srb = new SRBAccount();
                } else {
                    srb = new SRBAccount(mdasUserInfoDirectory);
                }
            }

            // if SRBAccount is still null, then throw sensible error.
            if (srb == null){
                // Cannot create SRBAccount with given data
                throw new FileSystemException("vfs.provider.srb/connect.error", host);
            }

            // Always try to specify the default storage resource - this is required so that
            // uploads are targeted to the correct default storage resource, otherwise, any
            // srb storage resource may be used and that can cause firewall block
            // (remeber, that the srb uses multiple storage resources).
            //srb.setDefaultStorageResource("ral-ngs1");
            if (defaultStorageResource != null) {
                srb.setDefaultStorageResource(defaultStorageResource);
            }
            // return SRBAccount.
            return srb;

        } catch (Exception ex) {
            throw new FileSystemException("vfs.provider.srb/connect.error", host, ex);
        }
    }



}