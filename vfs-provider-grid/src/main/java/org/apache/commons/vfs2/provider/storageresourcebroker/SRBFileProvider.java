package org.apache.commons.vfs2.provider.storageresourcebroker;

import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

/**
 * Author: Mathew Wyatt
 * Organisation: James Cook University
 * Date: Jun 6, 2008
 * Time: 10:44:24 AM
 */
public class SRBFileProvider extends AbstractOriginatingFileProvider {
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
    public SRBFileProvider() {
        super();
        setFileNameParser(SRBFileNameParser.getInstance());
    }

    public Collection getCapabilities() {
        return capabilities;
    }

    public final static UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[]{
        UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD
    };


	@Override
	protected FileSystem doCreateFileSystem(FileName rootName,
			FileSystemOptions fileSystemOptions) throws FileSystemException {
		 return new SRBVfsFileSystem(rootName, fileSystemOptions);
	}

}
