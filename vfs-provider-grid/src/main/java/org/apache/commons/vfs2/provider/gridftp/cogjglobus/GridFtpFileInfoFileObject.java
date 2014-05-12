package org.apache.commons.vfs2.provider.gridftp.cogjglobus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;
//import org.apache.commons.vfs.util.MonitorInputStream;
//import org.apache.commons.vfs.util.MonitorOutputStream;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
//import org.globus.ftp.vanilla.TransferState;
import org.globus.io.streams.GridFTPInputStream;
import org.globus.io.streams.GridFTPOutputStream;
import org.ietf.jgss.GSSCredential;

/**
 * Class representing a file in the GSIFTP File System.
 *
 * @deprecated Use {@link GridFtpMlsxFileObject} instead. GridFtpFileInfoFileObject
 *             class uses
 *             <a target="_blank" href="http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/FileInfo.html">
 *                org.globus.ftp.FileInfo
 *             </a>
 *             objects to represent remote files. However, according to
 *             <a target="_blank" href="http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/FTPClient.html#list()">
 *               globus java cog kit
 *             </a>
 *             it is strongly recommended to use
 *             <a target="_blank" href="http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/MlsxEntry.html">
 *                org.globus.ftp.MlsxEntry.MslxEntry
 *             </a>
 *             objects instead.
 *
 * @author David Meredith
 * @author Jos Koetsier
 *
 */
@Deprecated
public class GridFtpFileInfoFileObject extends AbstractFileObject {

    private static final Log log = LogFactory.getLog(GridFtpFileInfoFileObject.class);
    private GridFtpFileSystem fileSystem = null;
    private String relPath;
    private Map<String, FileInfo> children;
    private FileInfo fileInfo;
    private boolean inRefresh = false;
    private FileObject linkDestination;
    private static final Map<String, FileInfo> EMPTY_MAP = Collections.unmodifiableMap(new TreeMap<String, FileInfo>());

    /**
     * Constructor for the GSIFTP File Object.
     *
     * @param name Path to the file
     * @param fileSystem filesystem of the file-Xlint:unchecked
     * @param rootName root filenam
     * @throws org.apache.commons.vfs.FileSystemException
     */
    protected GridFtpFileInfoFileObject(final AbstractFileName name,
            final GridFtpFileSystem fileSystem, final FileName rootName) throws FileSystemException {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        //log.debug("rootName: "+rootName.toString());
        //log.debug("name: "+name.toString());
        //log.debug("relName: "+rootName.getRelativeName(name));
        this.relPath = UriParser.decode(rootName.getRelativeName(name));

        if (".".equals(relPath)) {
            this.relPath = null;
        }
    //log.debug("construct for " + getName().getPath() + " relPath: " + this.relPath);
    }

    /**
     * This will prepare the fileObject to get resynchronized with
     * the underlying filesystem if required
     *
     * @throws FileSystemException
     */
    @Override
    public void refresh() throws FileSystemException {
        if (!inRefresh) {
            log.debug("refresh for: " + getName().getPath());
            try {
                inRefresh = true;
                super.refresh();
                try {
                    // this will tell the parent to recreate its children collection
                    this.getInfo(true);
                } catch (IOException e) {
                    throw new FileSystemException(e);
                }
            } finally {
                inRefresh = false;
            }
        }
    }

    /**
     * Detaches this FileObject from its file resource
     * Is Called when this file is closed.
     * Note that this FileObject may be reused later, so should be able to be reattached
     * If not overidden, default impl does nothing.
     */
    @Override
    protected void doDetach() {
        log.debug("doDetach() of: " + getName().getPath());
        //nullify fileInfo or children loses the cache
        this.fileInfo = null;
        this.children = null;
    }

    /**
     * Attaches this file object to its file resource.
     * (i.e. it sets this.fileInfo  )
     * This method is called before any of the doBlah() or onBlah() methods.
     * Sub-classes can use this method to perform lazy initialisation.
     */
    @Override
    protected void doAttach() throws IOException {
        String absPath = UriParser.decode(getName().getPath());
        log.debug("doAttach() of: " + absPath);
        this.getInfo(false);
    }


    /**
     * If this file is a link, resolve to the destination.
     *
     * @return FileObject of the file this object links to.
     * @throws org.apache.commons.vfs.FileSystemException
     */
    private FileObject getLinkDestination() throws FileSystemException {
        if (linkDestination == null) {
            String name = this.fileInfo.getName();
            int end = name.indexOf(" -> ");
            if (end > 0) {
                name = name.substring(end + 4);
            }
            linkDestination = getFileSystem().resolveFile(name);
        }
        return linkDestination;
    }

    /**
     * Returns the file's list of children.
     *
     * @return The list of children
     * @throws FileSystemException If there was a problem listing children
     * @see AbstractFileObject#getChildren()
     * @since 1.0
     */
    @Override
    public FileObject[] getChildren() throws FileSystemException {
        try {
            //Wrap our parent implementation, noting that we're refreshing so
            //that we don't refresh() ourselves and each of our parents for
            //each children. Note that refresh() will list children. Meaning,
            //if if this file has C children, P parents, there will be (C * P)
            //listings made with (C * (P + 1)) refreshes, when there should
            //really only be 1 listing and C refreshes.

            this.inRefresh = true;
            // super class calls doListChildrenResolved(), if retruns null,
            // then super class calls doListChildren()
            return super.getChildren();
        } finally {
            this.inRefresh = false;
        }
    }

    /**
     * If this file is a link, return its children.
     * Called before the doListChildren to see if the client
     * can get access to the children directly
     *
     * @return Array of children if this object is a link, null otherwise.
     * @throws java.lang.Exception
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        //log.debug("doListChildrenResolved()");
        if (this.fileInfo.isSoftLink()) {
            return getLinkDestination().getChildren();
        } else {
            return null;
        }
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception {
        //log.debug("doListChildren()");
        doGetChildren();

        // TODO - get rid of this children stuff
        final String[] childNames = new String[children.size()];
        int childNum = -1;
        Iterator<FileInfo> iterChildren = children.values().iterator();
        while (iterChildren.hasNext()) {
            childNum++;
            final FileInfo child = iterChildren.next();

            if (child.isSoftLink()) {
                String name = child.getName();
                int end = name.indexOf(" -> ");
                if (end > 0) {
                    name = name.substring(0, end);
                    childNames[childNum] = name;
                }
            } else {
                childNames[childNum] = child.getName();
            }
        }
        return childNames;
    }

    /**
     * Fetches the children of this file, if not already cached.
     */
    private void doGetChildren() throws IOException {
        if (this.children != null) {
            return;
        }

        log.debug("=====> listing gridftp server: " + getName().getPath() + " relPath: " + this.relPath);

        final org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            client.setPassive();
            client.setLocalActive();
            client.setType(GridFTPSession.TYPE_ASCII);

            // prev
            //String previousDir = client.getCurrentDir();
            //client.changeDir((relPath == null) ? previousDir : relPath);
            //final List<FileInfo> childrenList = (List<FileInfo>) client.list();
            //client.changeDir(previousDir);
            //log.debug("======> changeDir: "+previousDir + " "+ relPath);


            // TODO according to http://www-unix.globus.org/cog/jftp/guide.html
            // the FTPClient must have type TYPE_ASCII to list directories
            // (although it seem to work ok as TYPE_IMAGE).
            // according to http://www-unix.globus.org/cog/distribution/1.4/API.html
            // and http://www-unix.globus.org/cog/distribution/1.4/api/org/globus/ftp/FTPClient.html#mlsd(java.lang.String)
            // should use msld() instead of list(), which returns vector of
            // MlsxEntry objects representing remote files rather than FileInfo objects

            //String previousDir = client.getCurrentDir();
            String listDir = getName().getPath() == null ? "/" : getName().getPath();
            client.changeDir(listDir);
            final List<FileInfo> childrenList = (List<FileInfo>) client.list();

            if (childrenList == null || childrenList.size() == 0) {
                children = GridFtpFileInfoFileObject.EMPTY_MAP;
            } else {
                log.debug("children are present: " + childrenList.size());
                children = new TreeMap<String, FileInfo>();

                // Remove '.' and '..' elements
                for (int i = 0; i < childrenList.size(); i++) {
                    final FileInfo child = childrenList.get(i);
                    if (child == null) {
                        continue;
                    }
                    String childname = child.getName();
                    //log.debug("child: " + childname);
                    if (!".".equals(childname) && !"..".equals(childname) &&
                            !"./".equals(childname) && !"../".equals(childname)) {
                        if (child.isSoftLink()) {
                            // need to parse softlinks in order to extract just the
                            // soft link name, as the name
                            // of the soft link must be added to the list of children
                            // otherwise recursive directory listing can fail (e.g.
                            // if listing a parent dir which is itself a soft link)!
                            int end = childname.indexOf(" -> ");
                            if (end > 0) {
                                childname = childname.substring(0, end);
                            }
                        }
                        children.put(childname, child);
                    }
                }
            }
        } catch (ClientException ex) {
            log.debug("ClientException: " + ex);//ex.printStackTrace();
            throw new IOException(ex.getMessage());
        } catch (ServerException ex) {
            log.debug("ServerException: " + ex); //ex.printStackTrace();
            throw new IOException(ex.getMessage());
        } finally {
            this.fileSystem.putClient(client);
        }
    }

    /**
     * Sets file attributes
     *
     * @param attrName unused.
     * @param value 3 digits. 0-7 each.
     */
    @Override
    protected void doSetAttribute(String attrName, Object value) throws FileSystemException {
        if (value instanceof String) {
            String mode = (String) value;
            final org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = this.fileSystem.getClient();

            try {
                client.setPassive();
                client.setLocalActive();
                //client.setType(Session.TYPE_ASCII);
                client.chmod(relPath, mode);
            } catch (Exception ex) {
            } finally {
                this.fileSystem.putClient(client);
            }
        }
    }

    /**
     * Updates this.fileInfo + parent.children (if flush is true,
     * parent.children is guranteed to be updated/refreshed)
     * It does this by listing the parent directory in order to get the
     * required file attributes for the requested (child) file.
     *
     * @param flush Reloads the cache if true
     */
    private void getInfo(boolean flush) throws IOException {
        log.debug("getInfo() for: " + getName().getPath());
        // getParent() constructs + gets the parent FO that contains this file.
        // getParent() is recursive, geting each of its (abstract) FO parents !
        // It Returns null if this file is the root of a file system.
        final GridFtpFileInfoFileObject parent = (GridFtpFileInfoFileObject) FileObjectUtils.getAbstractFileObject(getParent());


        //String pp = this.getName().getPath(); //get the parent path.
        //final GridFtpFileObject parent2 = (GridFtpFileObject) this.fileSystem.resolveFile(pp);

        FileInfo newFileInfo;
        if (parent != null) {
            // Updates this.fileInfo + parent.children by listing parent FO,
            // and extracts relevant FileInfo object in order to update
            // this.fileInfo ! In doing this, parent.children is also updated and cached.
            // (note, because getChildFile() is performed on a Parent
            // instance, no other info for 'this' class will be updated/cached,
            // e.g. this.children does not get updated). This is v.important !
            newFileInfo = parent.getChildFile(UriParser.decode(getName().getBaseName()), flush);
        } else {
            // Assume the root is a directory and exists
            newFileInfo = new FileInfo();
            newFileInfo.setName("/");
            newFileInfo.setFileType(FileInfo.DIRECTORY_TYPE);
        }
        this.fileInfo = newFileInfo;
    }

    /**
     * Get child of this fileInfo object with the given name.
     * Called by PARENT file objects, in order to locate the requested child
     * from their fileInfo objects.
     *
     * @param name file name of the child.
     * @param flush if true, reload the cache of children
     * @return child that was requested.
     * @throws java.io.IOException
     */
    private FileInfo getChildFile(final String name, final boolean flush) throws IOException {
        /* If we should flush cached children, clear our children map unless
         * we're in the middle of a refresh (i.e. inRefresh == true)
         * in which case we've just recently refreshed our children.
         * No need to do it again when our children are
         * refresh()ed calling getChildFile() for themselves from within
         * getInfo(). See getChildren(). */
        if (flush && !this.inRefresh) {
            children = null;
        }

        // List the children of this file
        doGetChildren();
        // Look for the requested child
        FileInfo info = this.children.get(name);
        return info;
    }

    /**
     * Called when the children of this file change.
     */
    @Override
    protected void onChildrenChanged(FileName child, FileType newType) {
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
            // TODO - get rid of this
            children = null;
        }
    }

    /**
     * Called when the type or content of this file changes.
     */
    @Override
    protected void onChange() throws IOException {
        children = null;
        if (FileType.IMAGINARY.equals(this.getType())) {
            // file is deleted, avoid server lookup
            log.debug("onChange() for (true): " + getName().getPath());
            this.fileInfo = null;
            return;
        }
        log.debug("onChange() for (false): " + getName().getPath());
        getInfo(true);
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception {
        if (this.fileInfo == null) {
            log.debug("FileInfo null for: " + getName().getPath());
            return FileType.IMAGINARY;
        } else if (this.fileInfo.isSoftLink()) {
            return getLinkDestination().getType();
        } else if (this.fileInfo.isDirectory()) {
            return FileType.FOLDER;
        } else if (this.fileInfo.isFile()) {
            return FileType.FILE;
        }

        throw new FileSystemException("vfs.provider.ftp/get-type.error", getName());
    }

    /**
     * Determines if this file can be written to.
     * Is only called if doGetType() does not return FileType.IMAGINARY.
     * Default implementation always returns true.
     */
    @Override
    protected boolean doIsWriteable() throws FileSystemException {
        // Note, in SRB, for collections (FOLDERS) fileInfo.userCanWrite() returns false
        // and this impl of doISWriteable() needs to return true for various
        // operations (e.g. doIsWriteable() is called before doCreateFolder(),
        // and so this method needs to return true) - thus only check on FILES
        //boolean w = fileInfo.userCanWrite();
        //log.debug("doIsWriteable() "+w+" "+getName().getPath());
        //return w;
        if (getType().equals(FileType.FILE)) {
            try {
                return fileInfo.userCanWrite();
            } catch (Exception ex) {
            }
        }
        return true;
    }

    /**
     * Determines if this file can be read. Is only called if doGetType()
     * does not return FileType.IMAGINARY.
     * Default implementation always returns true.
     */
    @Override
    protected boolean doIsReadable() throws FileSystemException {
        // Note, in SRB, for collections (FOLDERS) fileInfo.userCanRead() returns false
        // and this imple of doISWriteable() needs to return true for various
        // operations (e.g. for various recursive operations where this
        // this method needs to return true) - thus only check on FILES
        //boolean r = fileInfo.userCanRead();
        //log.debug("doIsReadable() "+r);
        //return r;
        if (getType().equals(FileType.FILE)) {
            try {
                return fileInfo.userCanRead();
            } catch (Exception ex) {
            }
        }
        return true;
    }

    /**
     * Get last modified time of this file.
     * Return number of millisecs since Jan 1st 1970 GMT.
     * Is only called if doGetType() does not return FileType.IMAGINARY.
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        try {
            if (this.fileInfo.isSoftLink()) {
                return getLinkDestination().getContent().getLastModifiedTime();
            } else {
                String timestamp = fileInfo.getDate() + " " + fileInfo.getTime();
                if (timestamp == null) {
                    return 0L;
                } else {
                    // Unix ls date/time (e.g. Mar 29 12:45)
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
                    // get number of millisecs since Jan 1 1970 GMT
                    // TODO - because the timestamp is limitted (no year), the
                    // year will always be 1970 which is wrong !
                    return sdf.parse(timestamp).getTime();
                }
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Sets the last modified time of this file or directory
     * Is only called if doGetType() does not return FileType.IMAGINARY.
     */
    //@Override
    protected boolean doSetLastModTime(long modtime)
            throws Exception {
        try {
            String modtimeString = String.valueOf(modtime);
            this.fileInfo.setTime(modtimeString);
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * Gets the size of this file.
     * @return size of file.
     * @throws java.lang.Exception
     */
    protected long doGetContentSize() throws Exception {
        return this.fileInfo.getSize();
    }

    /**
     * Deletes the file. Is only called when:
     * - doGetType() does not return FileType.IMAGINARY.
     * - doIsWriteable() returns true.
     * - This file has no children, if a folder.
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void doDelete() throws Exception {
        log.debug("doDelete() " + getName());
        final org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            if (FileType.FILE.equals(this.getType())) {
                client.deleteFile(getName().getPath());

            } else if (FileType.FOLDER.equals(this.getType())) {
                client.deleteDir(getName().getPath());
            }
        } finally {
            this.fileSystem.putClient(client);
        }
        this.fileInfo = null;
        this.children = EMPTY_MAP;
    }

    /**
     * Renames the file. Is only called when:
     * doIsWriteable() returns true.
     */
    @Override
    protected void doRename(FileObject newfile) throws Exception {
        String oldPath = getName().getPath();
        String newPath = newfile.getName().getPath();
        org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = this.fileSystem.getClient();
        try {
            client.rename(oldPath, newPath);
        } finally {
            this.fileSystem.putClient(client);
        }
        this.fileInfo = null;
        this.children = null;
    }

    @Override
    protected void doCreateFolder() throws FileSystemException {
        final org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = this.fileSystem.getClient();
        //client.makeDir(this.relPath);
        boolean ok = true;
        try {
            client.makeDir(getName().getPath());
        } catch (IOException ioe) {
            ok = false;
        } catch (ServerException se) {
            ok = false;
        } finally {
            this.fileSystem.putClient(client);
        }

        if (!ok) {
            throw new FileSystemException("vfs.provider.gsiftp/create-folder.error", getName());
        }
    }

    /**
     * Creates this file, if it does not exist.
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

    protected InputStream doGetInputStream() throws Exception {
        org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = null;
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

    /* // using inner class approach (as commented out below) was buggy.
    InputStreamDataSink sink = new InputStreamDataSink();
    //TransferState state = client.asynchGet(this.relPath, sink, null); // commented out
    TransferState state = client.asynchGet(getName().getPath(), sink, null);
    return new GSIFTPInputStream(client, state, sink.getInputStream());
     */
    }

    @Override
    protected OutputStream doGetOutputStream(boolean append) throws Exception {
         org.apache.commons.vfs2.provider.gridftp.cogjglobus.ExtendedGridFTPClient client = null;
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
    // using inner class approach (as commented out below) was buggy.
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
