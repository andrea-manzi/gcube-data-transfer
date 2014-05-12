package org.apache.commons.vfs2.provider.storageresourcebroker;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sdsc.SDSCSystemConfigBuilder;

import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFileSystem;

/**
 * Author: Mathew Wyatt
 * Organisation: James Cook University
 * Date: Jun 5, 2008
 * Time: 11:44:50 AM
 */
public class SRBFileSystemConfigBuilder extends SDSCSystemConfigBuilder {
    private final static String SRBPORTMAX = SRBFileSystemConfigBuilder.class.getName() + ".SRBPORTMAX";
    private final static String SRBPORTMIN = SRBFileSystemConfigBuilder.class.getName() + ".SRBPORTMIN";
    private final static String MDASDIR = SRBFileSystemConfigBuilder.class.getName() + ".MDASDIR";
    private final static String MDASDOMAIN = SRBFileSystemConfigBuilder.class.getName() + ".MDASDOMAIN";
    private final static String USELOCALMDASFILES = SRBFileSystemConfigBuilder.class.getName() + ".USELOCALMDASFILES";

    /**
     * The SrbFileSystemConfigBuilder.
     * Set/provide either a fully configured <code>edu.sdsc.grid.io.srb.SRBAccount</code>,
     * or provide connection related information that used to create an
     * <code>edu.sdsc.grid.io.srb.SRBAccount</code> when connecting to the SRB.
     *
     * <p>
     * SRB Connection Info used to create an <code>SRBAccount</code>
     * =============================================================
     *
     * + Host:Port
     * + Port range (portMin, portMax)
     *
     * if(GSI)
     *      + GSSCredential
     *      +/- homeDirectory
     *      +/- defaultStorageResource (if ommited, may not be able to perform writes)
     *
     * if(ENCRYPT1)
     *      + username (give in URI or via <code>StaticUserAuthenticator</code>)
     *      + password (give in URI or via <code>StaticUserAuthenticator</code>)
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
     *  @return static singleton
     */

    /**
     * The instance of this class
     */
    private final static SRBFileSystemConfigBuilder builder = new SRBFileSystemConfigBuilder();

    /**
     * Returns the single instance of this class
     * @return
     */
    public static SRBFileSystemConfigBuilder getInstance() {
        return builder;
    }

    /**
     * Sets an SRBAccount for use in the connection
     * @param account
     * @param opts
     */
    public void setAccount(SRBAccount account, FileSystemOptions opts) {
        setParam(opts, ACCOUNT, account);
    }

    /**
     * Gets the SRB account set for this connection
     * @param opts
     * @return
     */
    public SRBAccount getAccount(FileSystemOptions opts) {
        return (SRBAccount) getParam(opts, ACCOUNT);
    }

    /**
     * Returns a .class for the SRBFileSystemConfigBuilder
     * @return
     */
    protected Class getConfigClass() {
        return SRBFileSystem.class;
    }

    public void setMdasDomainName(FileSystemOptions options,
            String mdasDomainName) {
        setParam(options, MDASDOMAIN, mdasDomainName);
    }

    public String getMdasDomainName(FileSystemOptions options) {
        return (String) getParam(options, MDASDOMAIN);
    }

    public void setUseLocalMdasFiles(FileSystemOptions options,
            boolean useLocalMdasFiles) {
        setParam(options, USELOCALMDASFILES, useLocalMdasFiles ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean getUseLocalMdasFiles(FileSystemOptions options) {
        Object val = getParam(options, USELOCALMDASFILES);
        if (val != null) {
            return (Boolean) val;
        } else
            return true;
    }

    public void setMdasUserInfoDirectory(FileSystemOptions options,
            String userInfoDirectory) {
        setParam(options, MDASDIR, userInfoDirectory);
    }

        public String getMdasUserInfoDirectory(FileSystemOptions opts) {
        return (String) getParam(opts, MDASDIR);
    }

    public void setFileWallPortMax(FileSystemOptions options, int max) {
        setParam(options, SRBPORTMAX, new Integer(max));
    }

    public Integer getFileWallPortMax(FileSystemOptions opts) {
        return (Integer) getParam(opts, SRBPORTMAX);
    }

    public void setFileWallPortMin(FileSystemOptions options, int min) {
        setParam(options, SRBPORTMIN, new Integer(min));
    }

    public Integer getFileWallPortMin(FileSystemOptions opts) {
        return (Integer) getParam(opts, SRBPORTMIN);
    }
}
