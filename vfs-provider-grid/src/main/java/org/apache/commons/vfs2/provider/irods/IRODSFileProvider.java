package org.apache.commons.vfs2.provider.irods;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmak
 * Date: Dec 4, 2008
 * Time: 12:43:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class IRODSFileProvider extends AbstractOriginatingFileProvider
{
//funtionality these bindings offer
    protected static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]{
            Capability.CREATE,
            Capability.DELETE,
            Capability.RENAME,
            Capability.GET_TYPE,
            Capability.GET_LAST_MODIFIED,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT,
            Capability.URI,
            Capability.WRITE_CONTENT,
    }));

    /**
     * Constructor, set a FileNameParser
     */
    public IRODSFileProvider() {
        super();
        setFileNameParser(IRODSFileNameParser.getInstance());
    }

    /**
     * Creates a SRBVfsFileSystem
     * @param fileName
     * @param fileSystemOptions
     * @return
     * @throws FileSystemException
     */
    protected FileSystem doCreateFileSystem(FileName fileName, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return new IRODSVfsFileSystem(fileName, fileSystemOptions);
    }

    public Collection getCapabilities() {
        return capabilities;
    }
}
