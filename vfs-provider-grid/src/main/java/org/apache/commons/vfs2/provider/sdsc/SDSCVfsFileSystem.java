package org.apache.commons.vfs2.provider.sdsc;

import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import edu.sdsc.grid.io.RemoteFile;


/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 5, 2008
 * Time: 8:24:44 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class SDSCVfsFileSystem extends AbstractFileSystem
{
    protected Map<String, Object> attribs;
    /**
     * Used to hold resource to zone mappings
     */
    private Map<String, String> zoneResources = new HashMap<String, String>();

    /**
     * The key used to get the SRBFileSystem of this connection via the SRBVfsFileSystem.getAttribute(..) function
     */
    public static final String FILESYSTEM = "FILESYSTEM";

    /**
     * The key used to get the SRBAccount of this connection via the SRBVfsFileSystem.getAttribute(..) function
     */
    public static final String ACCOUNT = "ACCOUNT";

    public static final String HOME_DIRECTORY = "HOME_DIRECTORY";

        
    protected SDSCVfsFileSystem(final FileName rootName, final FileSystemOptions opts)
    {
        super(rootName, null, opts);
        attribs = new HashMap<String, Object>();
    }

    abstract public String getDefaultStorageResource();

    /**
     * Returns the current SRB storage resource for a zone determined from the SRBFile
     * @return
     */
    public String getResource(RemoteFile srbFile) {
        String filePath = srbFile.getPath();
        StringTokenizer tokens = new StringTokenizer(filePath, "/");

        //the zone is the 1st token
        if(tokens.hasMoreTokens()) {
            String resource = zoneResources.get(tokens.nextToken());
            if(resource != null) {
                return resource;
            }
        }

        //last ditch effort
        return getDefaultStorageResource();
    }

    /**
     * Sets a default resource for a zone
     * @param zoneName
     * @param resourceName
     */
    public void attachResourceToZone(String zoneName, String resourceName) {
        zoneResources.put(zoneName, resourceName);
    }

    /**
     * Get extra attributes from the filesystem connection
     * @param attrName
     * @return
     * @throws org.apache.commons.vfs.FileSystemException
     */
    public Object getAttribute(final String attrName) 	throws FileSystemException
    {
        return attribs.get(attrName);
    }

    /**
     * Set an attribute for this filesystem connection
     * @param attrName
     * @param o
     * @throws FileSystemException
     */
    public void setAttribute(String attrName, Object o) throws FileSystemException {
        attribs.put(attrName, o);
    }
}
