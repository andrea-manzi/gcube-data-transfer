package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
//import org.apache.commons.vfs.util.MonitorOutputStream;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
//import org.globus.ftp.vanilla.TransferState;
import org.globus.io.streams.GridFTPInputStream;
import org.globus.io.streams.GridFTPOutputStream;
import org.ietf.jgss.GSSCredential;

/**
 * Class representing a file in the GSIFTP File System.
 * This class uses
 * <a target="_blank" href="http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/MlsxEntry.html">
 * org.globus.ftp.MlsxEntry.MslxEntry
 * </a>
 * objects to represent remote gridftp files as recommended by
 * <a target="_blank" href="http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/FTPClient.html#list()">
 * globus java cog kit
 * </a>
 * rather than
 * <a target="_blank" href="http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/FileInfo.html">
 * org.globus.ftp.FileInfo
 * </a>
 * objects
 *
 * @author David Meredith
 *
 */
public class GridFtpMlsxFileObject extends AbstractFileObject {

    private static final Log log = LogFactory.getLog(GridFtpMlsxFileObject.class);
    private GridFtpFileSystem fileSystem = null;
    private String relPath;
    private static final Map<String, GridFtpMlsxFileObject> EMPTY_MAP = Collections.unmodifiableMap(new TreeMap<String, GridFtpMlsxFileObject>());
    // The format of the datetime returned by MlsxEntry MODIFY fact.
    // Account for two possible formats given by the server (df takes precidence)
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss.S");
    //private boolean inRefresh = false;
    //
    // cached info
    private Map<String, GridFtpMlsxFileObject> children = null;
    private MlsxEntry file;

    /**
     * Constructor
     *
     * @param name Path to the file
     * @param fileSystem filesystem of the file
     * @param rootName root filenam
     * @throws org.apache.commons.vfs.FileSystemException
     */
    protected GridFtpMlsxFileObject(final AbstractFileName name,
            final GridFtpFileSystem fileSystem, final FileName rootName) throws FileSystemException {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        //log.debug("rootName: "+rootName.toString());
        //log.debug("name: "+name.toString());
        //log.debug("relName: "+rootName.getRelativeName(name));
        //log.debug("construct for " + getName().getPath());
        this.relPath = UriParser.decode(rootName.getRelativeName(name));
        if (".".equals(relPath)) {
            this.relPath = null;
        }
    }

    /**
     * This will prepare the fileObject to get resynchronized with
     * the underlying filesystem if required
     *
     * @throws FileSystemException
     */
    /*@Override
    public void refresh() throws FileSystemException {
    if (!inRefresh) {
    log.debug("refresh for: " + getName().getPath());
    try {
    inRefresh = true;
    super.refresh();
    try {
    // true is to also tell the parent to recreate its children collection
    this.getInfo(true);
    } catch (IOException e) {
    throw new FileSystemException(e);
    }
    } finally {
    inRefresh = false;
    }
    }
    }*/
    /**
     * Used to force this file object re-inistatiate (refresh) itself
     *
     * @throws org.apache.commons.vfs.FileSystemException
     */
    public void forceGetInfo() throws FileSystemException {
        this.getInfo();
    }

    /**
     * Detaches this FileObject from its file resource
     * Is Called when this file is closed.
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doDetach()
     */
    @Override
    protected void doDetach() {
        // Note that this FileObject may be reused later, so should be able to be reattached
        // If not overidden, default impl does nothing.
        log.debug("doDetach() of: " + getName().getPath());
        //nullifying file or children loses the cache
        this.file = null;
        this.children = null;
    }

    /**
     * Attaches this file object to its file resource.
     * This method is called before any of the doBlah() or onBlah() methods.
     *
     * @throws org.apache.commons.vfs.FileSystemException if cannot retrieve
     * information for this file from the Gridftp server
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doAttach()
     */
    @Override
    protected void doAttach() throws FileSystemException {
        // Sub-classes can use this method to perform lazy initialisation.
        log.debug("doAttach() of: " + getName().getPath());
        // get parent (is created if not already cached, which invokes
        // constructor for FileObject - but not its doAttach() !).
        GridFtpMlsxFileObject parent = (GridFtpMlsxFileObject) getParent();
        //log.debug("Try find self in parent children in doAttach(): " + parent.getName());
        if (parent != null && parent.children != null) {
            final GridFtpMlsxFileObject self = parent.children.get(getName().getBaseName());
            if (self != null) {
                log.debug("Found self in parent children");
                this.file = self.getFile();
            }
        }
        if (this.file == null) {
            getInfo();
        }
    }

    /**
     * Returns the local file that this file object represents.
     *
     * @return the MlsxEntry file of this FileObject
     */
    protected MlsxEntry getFile() {
        return file;
    }

    private void getInfo() throws FileSystemException {
        log.debug("getInfo() for: " + getName().getPath());
        final ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {

            client.setPassive();
            client.setLocalActive();
            client.setType(GridFTPSession.TYPE_ASCII);

            String parDir = null;
            String active = null;
            if (getName().getParent() == null) {
                // if parent is null, we are listing the root dir '/'
                parDir = getName().getPath();
                active = getName().getPath();
            } else {
                parDir = getName().getParent().getPath() == null ? "/" : getName().getParent().getPath();
                active = getName().getBaseName();
            }

            client.changeDir(parDir);
            // mlst throws ServerException with err code=1 if file does not exist on server
            this.file = client.mlst(active);
            //this.file = client.mlst(getName().getPath());

            // set this FileObjects FileType by injection
            if (MlsxEntry.TYPE_DIR.equals(file.get(MlsxEntry.TYPE))) {
                injectType(FileType.FOLDER);

            } else if (MlsxEntry.TYPE_FILE.equals(file.get(MlsxEntry.TYPE))) {
                injectType(FileType.FILE);

            } else {
                log.debug("Not found on server: ");
                injectType(FileType.IMAGINARY);
            }
        } catch (org.globus.ftp.exception.ServerException ex) {
            // Need to catch server exceptions thrown when cannot either
            // change dir to the given dir on changeDir(parDir)
            // or when file does not exist on mslt(filename)
            //if(org.globus.ftp.exception.ServerException.SERVER_REFUSED == ex.getCode()){
            log.debug("Not found on server: [" + ex.getCode() + "] [" + ex.getCodeExplanation(ex.getCode()) + "]");
            injectType(FileType.IMAGINARY);
        //}
        } catch (Exception ex) {
            throw new FileSystemException("vfs.provider.gridftp/get-type.error", ex);
        } finally {
            this.fileSystem.putClient(client);
        }
    }

    /**
     * Determines the type of the file
     *
     * @return the type of file represented by this file object. Either
     * {@link FileType#IMAGINARY}, {@link FileType#FOLDER}, {@link FileType#FILE}
     * @throws org.apache.commons.vfs.FileSystemException if could not determine
     * the <code>FileType</code>
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetType()
     */
    protected FileType doGetType() throws FileSystemException {
        if (this.file == null) {
            return FileType.IMAGINARY;
        } else if (MlsxEntry.TYPE_DIR.equals(this.file.get(MlsxEntry.TYPE))) {
            return FileType.FOLDER;
        } else if (MlsxEntry.TYPE_FILE.equals(this.file.get(MlsxEntry.TYPE))) {
            return FileType.FILE;
        } else if (MlsxEntry.TYPE_CDIR.equals(this.file.get(MlsxEntry.TYPE))) {
            return FileType.IMAGINARY;
        } else if (MlsxEntry.TYPE_PDIR.equals(this.file.get(MlsxEntry.TYPE))) {
            return FileType.IMAGINARY;
        }
        throw new FileSystemException("vfs.provider.ftp/get-type.error", getName());
    }

    /**
     * Use #doListChildrenResolved() for performance
     *
     * @return null
     * @throws java.lang.Exception
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doListChildren()
     */
    protected String[] doListChildren() throws Exception {
        //log.debug("doListChildren() for: [" + getName().getBaseName() + "]");
        return null;
    }

    /**
     * Lists the children of this file.
     * Is only called if doGetType() returns FileType.FOLDER.
     * The return value of this method is cached,
     * so the implementation can be expensive.
     *
     * @return the FileObject array
     * @throws org.apache.commons.vfs.FileSystemException if an error occurs
     * listing the children of this FileObject
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doListChildrenResolved()
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws FileSystemException {
        // Called before the doListChildren to see if the client
        // can get access to the children directly
        log.debug("doListChildrenResolved() for: " + getName().getBaseName());
        // List the contents of the folder
        statChildren();
        return (GridFtpMlsxFileObject[]) children.values().toArray(new GridFtpMlsxFileObject[children.size()]);
    }

    /**
     * Fetches the children of this file, if not already cached.
     */
    private void statChildren() throws FileSystemException {
        if (this.children != null) {
            return;
        }
        log.debug("=====> listing gridftp server: " + getName().getPath() + " relPath: " + this.relPath);
        final ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            // According to http://www-unix.globus.org/cog/jftp/guide.html
            // the FTPClient must have type TYPE_ASCII to list directories
            // (although it seem to work ok as TYPE_IMAGE).
            //
            // According to http://www-unix.globus.org/cog/distribution/1.4/API.html
            // and http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/FTPClient.html#mlsd(java.lang.String)
            // we should use msld() instead of list(), which returns vector of
            // MlsxEntry objects representing remote files rather than FileInfo objects

            client.setPassive();
            client.setLocalActive();
            client.setType(GridFTPSession.TYPE_ASCII);

            //String previousDir = client.getCurrentDir();
            String listDir = getName().getPath() == null ? "/" : getName().getPath();
            client.changeDir(listDir);
            //final List<FileInfo> childrenList = (List<FileInfo>) client.list();
            final List<MlsxEntry> kids = (List<MlsxEntry>) client.mlsd();
            //client.changeDir(previousDir);

            if (kids == null || kids.size() == 0) {
                children = EMPTY_MAP;
            } else {
                log.debug("children are present: " + kids.size());
                children = new TreeMap<String, GridFtpMlsxFileObject>();

                for (int i = 0; i < kids.size(); i++) {
                    final MlsxEntry child = kids.get(i);
                    if (child == null) {
                        continue;
                    }
                    String childname = child.getFileName();
                    if (childname != null && //!".".equals(childname) && !"..".equals(childname) &&
                            !"./".equals(childname) && !"../".equals(childname)) {

                       // update the child FileObjects MslxEntry
                        FileName fn = null;
                        try {
                            // Resolves a name, relative to the "root" file name.
                            // Refer to NameScope  for a description of how names are resolved.
                            //
                            // Parameters:
                            // root - the base filename (which is'this' GridFtpMlsxFileObject which is the parent)
                            // name - The name to resolve.
                            // scope - The NameScope to use when resolving the name.
                            fn = getFileSystem().getFileSystemManager().
                                resolveName(getName(), UriParser.encode(childname), NameScope.CHILD);
                        }catch(Exception ex){
                            // VFS didn't like the child name for whatever reason,
                            // (prob an illegal file name char?).
                            // Therefore, don't bother adding it as a child as
                            // VFS will trip over if trying to resolve the child FileObject.
                            log.error("Skipping unresolvable child FileObject");
                        }
                        // Need to check that childname == fn.getBaseName()
                        // otherwise, will not be able to resolveFile correctly which causes ex.
                        if (fn != null && childname != null && childname.equals(fn.getBaseName())) {

                            GridFtpMlsxFileObject fo = (GridFtpMlsxFileObject) getFileSystem().resolveFile(fn);
                            //fo.file = client.mlst(childname);
                            fo.file = child;

                            // set this FileObjects FileType by injection
                            // ignore pdir and cdir
                            if (MlsxEntry.TYPE_DIR.equals(child.get(MlsxEntry.TYPE))) {
                                fo.injectType(FileType.FOLDER);
                                children.put(childname, fo);

                            } else if (MlsxEntry.TYPE_FILE.equals(child.get(MlsxEntry.TYPE))) {
                                fo.injectType(FileType.FILE);
                                children.put(childname, fo);
                            }
                        }

                    }
                }
            }
        } catch (IOException ex) {
            throw new FileSystemException("vfs.provider/list-children.error", ex);
        } catch (ClientException ex) {
            throw new FileSystemException("vfs.provider/list-children.error", ex);
        } catch (ServerException ex) {
            throw new FileSystemException("vfs.provider/list-children.error", ex);
        } finally {
            this.fileSystem.putClient(client);
        }
    }

    /**
     * Called when the type or content of this file changes.
     * Sets the #children to null
     *
     * @throws org.apache.commons.vfs.FileSystemException
     * @see #getInfo()
     * @see org.apache.commons.vfs.provider.AbstractFileObject#onChange()
     */
    @Override
    protected void onChange() throws FileSystemException {
        // Need to set the this.children to null which will cause the children
        // to be re-fetched if required.
        children = null;
        /*if (FileType.IMAGINARY.equals(this.getType())) {
        // file is deleted, avoid server lookup
        log.debug("onChange() for (true): " + getName().getPath());
        this.file = null;
        return;
        }
        log.debug("onChange() for (false): " + getName().getPath());
         */
        getInfo();
    }

    /**
     * Called when the children of this file change.
     * (Allows subclasses to refresh any cached information about the children of this file)
     *
     * @param child
     * @param newType
     * @see org.apache.commons.vfs.provider.AbstractFileObject#onChildrenChanged(FileName child, FileType newType)
     */
    @Override
    protected void onChildrenChanged(FileName child, FileType newType) {
        // e.g. called AFTER the deletion of a child (after the childs doDelete()
        // and onChange() have returned).
        if (children != null && newType.equals(FileType.IMAGINARY)) {
            log.debug("onChildrenChanged() for (true): " + getName().getPath());
            try {
                children.remove(UriParser.decode(child.getBaseName()));
            } catch (FileSystemException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.debug("onChildrenChanged() for (false): " + getName().getPath());
            // if child was added we have to rescan the children
            children = null;
        }
    }

    /**
     * Sets file attributes.
     * Is only called if doGetType()  does not return <code>FileType.IMAGINARY</code>
     *
     * @param attrName unused.
     * @param value 3 digit string (0-7 for each digit) reflecting the mode of
     * the file or dir.
     * @throws org.apache.commons.vfs.FileSystemException if could not get
     *         an established connection to GridFtp server.
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doSetAttribute(String attrName, Object value)
     */
    @Override
    protected void doSetAttribute(String attrName, Object value) throws FileSystemException {
        if (value instanceof String) {
            String mode = (String) value;
            final ExtendedGridFTPClient client = this.fileSystem.getClient();
            try {
                client.setPassive();
                client.setLocalActive();
                //client.setType(Session.TYPE_ASCII);
                client.chmod(relPath, mode);
            } catch (Exception ex) {
                log.error("Error: doSetAttribute", ex);
            } finally {
                this.fileSystem.putClient(client);
            }
        }
    }

    /**
     * Determines if this file/folder is writable.
     * Is only called if doGetType() does not return <code>FileType.IMAGINARY</code>
     *
     * @return return if <code>FileType.FILE</code>, return true if can write or append to file.
     * If <code>FileType.FOLDER</code>, return true if can create new sub directories.
     * Otherwise, return false;
     * @throws org.apache.commons.vfs.FileSystemException if cannot determine
     * <code>FileType</code> and write status
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doIsWriteable()
     */
    @Override
    protected boolean doIsWriteable() throws FileSystemException {
        // doIsWriteable() needs to return true for various
        // operations (e.g. doIsWriteable() is called before doCreateFolder(),
        // and so this method needs to return true in these scenarios)
        // MlsxEntry objects return RFC PERM facts that are different for
        // files and folders - see:
        // http://rfc-ref.org/RFC-TEXTS/3659/chapter7.html#d4e444590

        String perm = this.file.get(MlsxEntry.PERM);
        if (getType().equals(FileType.FILE)) {
            if (perm.contains("w") || perm.contains("W") || perm.contains("a") || perm.contains("A")) {
                return true; // write (STOR command allowed) or append
            } else {
                return false;
            }
        } else if (getType().equals(FileType.FOLDER)) {
            if (perm.contains("c") || perm.contains("C") ||
                    perm.contains("m") || perm.contains("M")) {
                return true; // create new files or make new sub-dirs ok
            } else {
                return false;
            }
        }
        // Default impl always returns true.
        return true;
    }

    /**
     * Determines if this file can be read. Is only called if #doGetType()
     * does not return <code>FileType.IMAGINARY</code>.
     *
     * @return true if can read file or navigate into folder, otherwise
     * return false.
     * @throws org.apache.commons.vfs.FileSystemException if cannot determine
     * <code>FileType</code> and read status
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doIsReadable()
     */
    @Override
    protected boolean doIsReadable() throws FileSystemException {
        // for FileInfo objects (boolean r = fileInfo.userCanRead());
        // MlsxEntry objects return RFC PERM facts that are different for
        // files and folders - see:
        // http://rfc-ref.org/RFC-TEXTS/3659/chapter7.html#d4e444590

        String perm = this.file.get(MlsxEntry.PERM);
        if (getType().equals(FileType.FILE)) {
            if (perm.contains("r") || perm.contains("R")) {
                return true; // read (RETR command allowed)
            } else {
                return false;
            }
        } else if (getType().equals(FileType.FOLDER)) {
            if (perm.contains("e") || perm.contains("E") ||
                    perm.contains("l") || perm.contains("L")) {
                return true; // enter or list dir allowed
            } else {
                return false;
            }
        }
        // Default impl always returns true.
        return true;
    }

    /**
     * Get last modified time of this file.
     * Is only called if doGetType() does not return <code>FileType.IMAGINARY</code>
     *
     * @return number of millisecs since Jan 1st 1970 GMT.
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() {
        try {
            String modify = this.file.get(MlsxEntry.MODIFY);
            Date d = df.parse(modify);
            return d.getTime();
        } catch (Exception ex) {
            // Possible that the SimpleDateFormat of the MlsxEntry
            // is not df. Therefore try more accurate datetime (df2).
            try {
                String modify = this.file.get(MlsxEntry.MODIFY);
                Date d2 = df2.parse(modify);
                return d2.getTime();
            } catch (Exception ex2) {
            // give up trying to parse datetime format.
            }
        }
        return 0L;
    }

    /**
     * Sets the last modified time of this file or directory
     * Is only called if doGetType() does not return <code>FileType.IMAGINARY</code>
     *
     * @param modtime number of millisecs since Jan 1st 1970 GMT.
     * @return true if successfully set modtime or false if not.
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doSetLastModTime(long modtime)
     */
    //@Override
    protected boolean doSetLastModTime(long modtime) {
        try {
            String modtimeString = String.valueOf(modtime);
            this.file.set(MlsxEntry.MODIFY, modtimeString);
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * Gets the size of this file.
     *
     * @return size of file in bytes.
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetContentSize()
     */
    protected long doGetContentSize() {
        try {
            String size = this.file.get(MlsxEntry.SIZE);
            //log.debug("doGetContentSize(): "+size);
            return Long.parseLong(size);
        } catch (Exception ex) {
            return 0l;
        }
    }

    /**
     * Deletes the file. Is only called when:
     * - doGetType() does not return <code>FileType.IMAGINARY</code>.
     * - doIsWriteable() returns true.
     * - This folder has no children (if <code>FileType.FOLDER</code>).
     *
     * @throws org.apache.commons.vfs.FileSystemException
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doDelete()
     */
    @Override
    protected void doDelete() throws FileSystemException {
        log.debug("doDelete() " + getName());
        final ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            if (FileType.FILE.equals(this.getType())) {
                client.deleteFile(getName().getPath());

            } else if (FileType.FOLDER.equals(this.getType())) {
                client.deleteDir(getName().getPath());
            }

        } catch (IOException ex) {
            throw new FileSystemException("vfs.provider.gridftp/delete-file.error", ex);
        } catch (ServerException ex) {
            throw new FileSystemException("vfs.provider.gridftp/delete-file.error", ex);
        } finally {
            this.fileSystem.putClient(client);
        }
        this.file = null;
        this.children = EMPTY_MAP;
    }

    /**
     * Renames the file. Is only called when:
     * doIsWriteable() returns true.
     *
     * @param newfile file or folder to rename
     * @throws org.apache.commons.vfs.FileSystemException
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doRename(FileObject newfile)
     */
    @Override
    protected void doRename(FileObject newfile) throws FileSystemException {
        String oldPath = getName().getPath();
        String newPath = newfile.getName().getPath();
        ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            client.rename(oldPath, newPath);
        } catch (IOException ex) {
            throw new FileSystemException("vfs.provider.gridftp/rename-file.error", ex);
        } catch (ServerException ex) {
            throw new FileSystemException("vfs.provider.gridftp/rename-file.error", ex);
        } finally {
            this.fileSystem.putClient(client);
        }
        this.file = null;
        this.children = null;
    }

    /**
     * Creates this file as a folder. Is only called when:
     * - doGetType() returns <code>FileType.IMAGINARY</code>
     * - The parent folder exists and is writeable, or this file is the root of the file system
     *
     * @throws org.apache.commons.vfs.FileSystemException
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doCreateFolder()
     */
    @Override
    protected void doCreateFolder() throws FileSystemException {
        final ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            client.makeDir(getName().getPath());
        } catch (IOException ex) {
            throw new FileSystemException("vfs.provider.gsiftp/create-folder.error", getName(), ex);
        } catch (ServerException ex) {
            throw new FileSystemException("vfs.provider.gsiftp/create-folder.error", getName(), ex);
        } finally {
            this.fileSystem.putClient(client);
        }
    }

    /**
     * Creates this file if it does not exist.
     *
     * @throws org.apache.commons.vfs.FileSystemException
     * @see org.apache.commons.vfs.provider.AbstractFileObject#createFile()
     */
    @Override
    public void createFile() throws FileSystemException {
        synchronized (this) {
            try {
                getOutputStream().close();
                endOutput();
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
            }
        }
    }

    /**
     * Creates an input stream to read the content from.
     *
     * @return InputStream for this FileObject
     * @throws java.lang.Exception if error creating InputStream
     */
    protected InputStream doGetInputStream() throws Exception {
        ExtendedGridFTPClient client = null;
        String host;
        Integer port;
        try {
            client = this.fileSystem.getClient();
            host = client.getHost();
            port = client.getPort();
        } finally {
            fileSystem.putClient(client);
        }

        client.setPassive();
        client.setLocalActive();
        //client.setPassiveMode(true);
        client.setType(GridFTPSession.TYPE_IMAGE);
        log.debug("Creating GridFTP Input Stream to: " + host + ":" + port + getName().getPath());
        GSSCredential proxy = (GSSCredential) this.fileSystem.getAttribute(GridFtpFileSystem.CREDENTIAL);
        return new GridFTPInputStream(proxy, host, port, getName().getPath());

    /* // using inner class approach (as commented out below) caused probs.
    InputStreamDataSink sink = new InputStreamDataSink();
    //TransferState state = client.asynchGet(this.relPath, sink, null); // commented out
    TransferState state = client.asynchGet(getName().getPath(), sink, null);
    return new GSIFTPInputStream(client, state, sink.getInputStream());
     */
    }

    /**
     * Creates an output stream to write the file content to. Is only called if:
     * #doIsWriteable() returns true,
     * #doGetType() returns {@link FileType#FILE}, or
     * #doGetType() returns {@link FileType.IMAGINARY}
     *
     * @param  append  if true, append to the file
     * @return the OutputStream to write file content to (does not have to be
     *         buffered).
     * @throws java.lang.Exception
     */
    @Override
    protected OutputStream doGetOutputStream(boolean append) throws Exception {
        ExtendedGridFTPClient client = null;
        String host;
        Integer port;
        try {
            client = this.fileSystem.getClient();
            host = client.getHost();
            port = client.getPort();
        } finally {
            fileSystem.putClient(client);
        }

        client.setPassive();
        client.setLocalActive();
        //client.setPassiveMode(true);
        client.setType(GridFTPSession.TYPE_IMAGE);
        log.debug("Creating GridFTP Output Stream to: " + host + ":" + port + getName().getPath());
        GSSCredential proxy = (GSSCredential) this.fileSystem.getAttribute(GridFtpFileSystem.CREDENTIAL);
        return new GridFTPOutputStream(proxy, host, port, getName().getPath(), append);

    /*
    // using inner class approach (as commented out below) caused probs.
    OutputStreamDataSource source = new OutputStreamDataSource(1024);
    //TransferState state = client.asynchPut(this.relPath, source, null, append);
    TransferState state = client.asynchPut(getName().getPath(), source, null, append);
    state.waitForStart();
    return new GSIFTPOutputStream(client, state, source.getOutputStream());
     */
    }

    /*class GSIFTPInputStream extends MonitorInputStream {
    private final ExtendedGridFTPClient client;
    private final TransferState state;
    public GSIFTPInputStream(final ExtendedGridFTPClient client,
    TransferState state, final InputStream in) {
    super(in);
    this.client = client;
    this.state = state;
    }
    //  Called after the stream has been closed.
    @Override
    protected void onClose() throws IOException {
    try {
    in.close();
    state.waitForEnd();
    } catch (Exception ex) {
    } finally {
    fileSystem.putClient(client);
    }
    }
    }*/

    //  An OutputStream that monitors for end-of-file.
    /*private class GSIFTPOutputStream extends MonitorOutputStream {
    private final ExtendedGridFTPClient client;
    private final TransferState state;
    public GSIFTPOutputStream(final ExtendedGridFTPClient client,
    TransferState state, final OutputStream out) {
    super(out);
    this.client = client;
    this.state = state;
    }
    //  Called after this stream is closed.
    @Override
    protected void onClose() throws IOException {
    try {
    out.close();
    state.waitForEnd();
    } catch (Exception ex) {
    } finally {
    fileSystem.putClient(client);
    }
    }
    }*/
}
