package org.apache.commons.vfs2.provider.sdsc;

import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.ietf.jgss.GSSCredential;

/**
 * Abstract class that contains common filesystem configurations for both SRB
 * and iRODS.  Code completely taken from David Meredith's original SRB provider
 */
abstract public class SDSCSystemConfigBuilder extends FileSystemConfigBuilder {
    protected final static String ACCOUNT = "ACCOUNT";
    protected final static String HOMEDIR = "HOMEDIR";
    protected final static String ZONE = "ZONE";
    protected final static String DEFAULTRESOURCE = "DEFAULTRESOURCE";
    private final static String GSSCREDENTIAL = "GSSCREDENTIAL";

    public void setHomeDirectory(FileSystemOptions options, String homeDir) {
        setParam(options, HOMEDIR, homeDir);
    }

    public String getHomeDirectory(FileSystemOptions options) {
        return (String) getParam(options, HOMEDIR);
    }

    public void setMcatZone(FileSystemOptions options,
            String zone) {
        setParam(options, ZONE, zone);
    }

    public String getMcatZone(FileSystemOptions options) {
        return (String) getParam(options, ZONE);
    }

    public void setDefaultStorageResource(FileSystemOptions options,
            String defaultResource) {
        setParam(options, DEFAULTRESOURCE, defaultResource);
    }

    public String getDefaultStorageResource(FileSystemOptions opts) {
        return (String) getParam(opts, DEFAULTRESOURCE);
    }

    public void setGSSCredential(FileSystemOptions options,
            GSSCredential credential) {
        setParam(options, GSSCREDENTIAL, credential);
    }

    public GSSCredential getGSSCredential(FileSystemOptions options) {
        return (GSSCredential) getParam(options, GSSCREDENTIAL);
    }
}
