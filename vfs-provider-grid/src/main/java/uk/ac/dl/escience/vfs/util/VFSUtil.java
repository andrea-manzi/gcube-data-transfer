/*
 * VFSUtil.java
 *
 * Created on 16 May 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.ac.dl.escience.vfs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.io.FileInputStream;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.provider.http.HttpFileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.commons.vfs2.provider.temp.TemporaryFileProvider;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.MarkerListener;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.apache.commons.vfs2.provider.gridftp.cogjglobus.GridFtpFileProvider;
import org.apache.commons.vfs2.provider.gridftp.cogjglobus.GridFtpFileSystem;
import org.apache.commons.vfs2.provider.gridftp.cogjglobus.GridFtpMlsxFileObject;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBFileProvider;

/**
 * @author David Meredith
 *
 * Class used to perform recursive copying between different file systems.
 * For two gridftp uri's, you can perform third party file transfers which means
 * bytes do not have to be piped through the VFS client.
 */
public class VFSUtil {

    private static Log log = LogFactory.getLog(VFSUtil.class);

    /**
     * Create and init a new DefaultFileSystemManager (not a singleton).
     *
     * @param ftp if true add new FtpFileProvider
     * @param sftp if true add new SftpFileProvider
     * @param http if true add new HttpFileProvider
     * @param gsiftp if true add new GridFtpFileProvider
     * @param srb if true add new SrbFileProvider
     * @param file if true add DefaultLocalFileProvider
     * @param tmpDirPath used for TemporaryFileProvider creation (uses
     * java.io.tmpdir if not give)
     * @return new instance
     * @throws org.apache.commons.vfs.FileSystemException
     */
    public static DefaultFileSystemManager createNewFsManager(
            boolean ftp, boolean sftp, boolean http, boolean gsiftp, boolean srb,
            boolean file, String tmpDirPath) throws FileSystemException {

        // note, fsManager is not a singleton here ! thus it is meant that
        // a fsManager is created per thread and close the fsManager
        DefaultFileSystemManager fsManager = new DefaultFileSystemManager();
        if (ftp) {
            fsManager.addProvider("ftp", new FtpFileProvider());
        }
        if (sftp) {
            fsManager.addProvider("sftp", new SftpFileProvider());
        }
        if (http) {
            fsManager.addProvider("http", new HttpFileProvider());
        }
        if (gsiftp) {
            fsManager.addProvider("gsiftp", new GridFtpFileProvider());
        }
        if (srb) {
            fsManager.addProvider("srb", new SRBFileProvider());
        }
        if (file) {
            fsManager.addProvider("file", new DefaultLocalFileProvider());
        }

        File xFile = null;
        if (tmpDirPath != null) {
            xFile = new File(tmpDirPath);
        } else {
            xFile = new File(System.getProperty("java.io.tmpdir", "/tmp"));
        }
        if (!xFile.exists()) {
            throw new IllegalStateException("cannot allocate temporary directory. Please set java.io.tmpdir system property");
        }
        fsManager.addProvider("tmp", new TemporaryFileProvider(xFile));
        fsManager.setTemporaryFileStore(new DefaultFileReplicator(xFile));
        fsManager.init();
        return fsManager;
    }

    public static GSSCredential loadProxy(String location) throws FileNotFoundException, IOException, GSSException {
        return loadProxy(new File(location));
    }

    public static GSSCredential loadProxy(File location) throws FileNotFoundException, IOException, GSSException {
        byte[] data = new byte[(int) location.length()];
        FileInputStream in = new FileInputStream(location);// read in the credential data
        in.read(data);
        in.close();

        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        GSSCredential cred = manager.createCredential(data, ExtendedGSSCredential.IMPEXP_OPAQUE,
                GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
        return cred;
    }

    /*public static void testFileSystemExceptionMessageOK() throws FileSystemException{
         if(true)
            throw new FileSystemException("vfs.provider/copy-missing-file.error");

    }*/


    /**
     * Copy the content of srcFo to destFo.
     * <p/>
     * 1) if srcFo is a file and destFo is a dir, then copy srcFo INTO destTo.
     * 2) if srcFo is a file and destFo is a file, then OVERWRITE destFo.
     * 3) if srcFo is a file and destFo is IMAGINARY, then create destFo file.
     * 4) if srcFo is a dir and destFo is a dir, then copy children of srcFo INTO destFo (*)
     * 5) if srcFo is a dir and destFo is IMAGINARY, then create destFo dir.
     * 6) if srcFo is a dir and destFo is a file, then throw IOException
     *
     *   <p>
     *   Copying the children of the srcFo directory into the destFo dir
     *   is preferable to copying the srcFo dir inclusive into the destFo dir.
     *   This is because the user may want to provide an alternative base
     *   name for the destFo dir (e.g. 'dirAcopy' rather than 'dirA').
     *   Do the following:
     *      destFo = obj.resolveFile("newDirName");
     *      if(!destFo.exists()) destFo.createFolder();
     *   </p>
     *
     * @param srcFo the source file to copy from
     * @param destFo the destination file to create/overwrite
     * @param doThirdPartyTransferForTwoGridFtpFileObjects If true and both srcFo
     * and destFo are gsiftp uris, then attempt a gridftp third party file transfer
     * (requires cog.properties), otherwise do byte IO (byte streaming)
     * between srcFo and destFo.
     *
     * @throws IOException if an error occurs on byte transfer
     * @throws org.apache.commons.vfs.FileSystemException if srcFo does not exist,
     * if srcFo is a directory and destFo exists and is a file,
     * if destFo is not writable
     *
     */
    public static void copy(FileObject srcFo, FileObject destFo, MarkerListener listener,
            boolean doThirdPartyTransferForTwoGridFtpFileObjects) throws IOException, FileSystemException {

        // todo: support append
        // check srcFo file exsits and is readable
        if (!srcFo.exists()) {
            throw new FileSystemException("vfs.provider/copy-missing-file.error", srcFo);
        }
        if(!srcFo.isReadable()){
            throw new FileSystemException("vfs.provider/read-not-readable.error", srcFo);
        }

        // create the destination file or folder if it does not already exist.
        if (destFo.getType() == FileType.IMAGINARY || !destFo.exists()) {
            if (srcFo.getType().equals(FileType.FILE)) {
                destFo.createFile();
            } else if (srcFo.getType().equals(FileType.FOLDER)) {
                destFo.createFolder();
            }
        }

        // check can write to the target
        if (!destFo.isWriteable()) {
            throw new FileSystemException("vfs.provider/copy-read-only.error", destFo);
        }

        // check src and target FileObjects are not the same file
        if(destFo.getName().getURI().equals(srcFo.getName().getURI())){
            throw new FileSystemException("vfs.provider/copy-file.error", new Object[]{srcFo, destFo}, null);
        }

        // Do transfer
        // If two gsiftp uris and electing to do third party transfer
        if (doThirdPartyTransferForTwoGridFtpFileObjects &&
                srcFo.getName().getScheme().equalsIgnoreCase("gsiftp") &&
                destFo.getName().getScheme().equalsIgnoreCase("gsiftp")) {
            try {
                GridFTPUtil util = setupGridFtpThridPartyTransfer(srcFo, destFo, listener);
                doGridFtpThridPartyTransfer(util, srcFo, destFo, false);
            } catch (Exception ex) {
                throw new IOException("Error on gridftp third party transfer");
            }
            return;
        }

        // Do transfer
        // If copying between two different file systems
        if (srcFo.getType().equals(FileType.FILE)) {
            if (destFo.getType().equals(FileType.FOLDER)) {
                log.debug("vfs FILE into FOLDER");
                // get a handle on the new file to create at the destination.
                FileObject nestedDestFo = destFo.resolveFile(srcFo.getName().getBaseName());
                // copyFileToFile(srcFo, nestedDestFo, false); //append false here
                nestedDestFo.copyFrom(srcFo, new AllFileSelector());

            } else {
                log.debug("vfs FILE to FILE");
                // copyFileToFile(srcFo, destFo, false); //append false here
                destFo.copyFrom(srcFo, new AllFileSelector());

            }
        } else if (srcFo.getType().equals(FileType.FOLDER)) {
            // copying the children of a folder into another folder
            if (destFo.getType().equals(FileType.FOLDER)) {
                log.debug("vfs FOLDER children into FOLDER");
                destFo.copyFrom(srcFo, new AllFileSelector());

            } else {
                throw new IOException("Cannot copy a folder to a destination that is not a folder");
            }
        } else {
            throw new IOException("Cannot copy from path of type " + srcFo.getType() + " to another path of type " + destFo.getType());
        }
    }

    /**
     * Performs a more efficient 3rd party file transfer between two gridftp
     * resources (taking advantage of gridftp parallel and striped transfers)
     * rather than buffering byte streams as in copy.
     *
     * @param srcFO
     * @param destFO
     * @throws java.io.IOException
     * @throws java.lang.Exception
     */
     static public GridFTPUtil setupGridFtpThridPartyTransfer(FileObject srcFO,
            FileObject destFO, MarkerListener listener) throws IOException, Exception {

        log.debug("setupGridFtpThridPartyTransfer()");

        GridFtpFileSystem srcFS = (GridFtpFileSystem) srcFO.getFileSystem();
        GridFTPClient srcClient = srcFS.getClient();
        GridFtpFileSystem destFS = (GridFtpFileSystem) destFO.getFileSystem();
        GridFTPClient destClient = destFS.getClient();
        // create a new GridFTPUtil as this class is not stateless !
        GridFTPUtil gridftpUtil = new GridFTPUtil();
        gridftpUtil.setSourceClient(srcClient);
        gridftpUtil.setDestClient(destClient);
        gridftpUtil.setMarkerListener(listener);
        return gridftpUtil;
    }

    static public  void doGridFtpThridPartyTransfer(GridFTPUtil gridftpUtil, FileObject srcFO,
            FileObject destFO, boolean append) throws IOException, Exception {

        GridFTPClient srcClient = gridftpUtil.getSourceClient();
        GridFTPClient destClient = gridftpUtil.getDestinationClient();

        if(srcClient == null)
        {
            GridFtpFileSystem srcFS = (GridFtpFileSystem) srcFO.getFileSystem();
            log.debug("Source client null - need to call getClient");
            srcClient = srcFS.getClient();
        }

        if(destClient == null)
        {
            GridFtpFileSystem destFS = (GridFtpFileSystem) destFO.getFileSystem();
            log.debug("destination client null - need to call getClient");
            destClient = destFS.getClient();
        }

        if (srcFO.getType().equals(FileType.FILE)) {
            if (destFO.getType().equals(FileType.FOLDER)) {
                // copying a file into a directory
                log.debug("gsiftp FILE into FOLDER");
                String srcFullpathFile = srcFO.getName().getPath();
                //String destFullpathFile = pTo.getName().getPath() + "/" + pFrom.getName().getBaseName();
                FileObject newFile = destFO.resolveFile(srcFO.getName().getBaseName());
                String destFullpathFile = newFile.getName().getPath();
                gridftpUtil.thirdPartyFileTransfer(srcClient, srcFullpathFile, destClient, destFullpathFile, append);

            } else {
                log.debug("gsiftp FILE to FILE");
                // copying a file to file (overwrite file or create if file is IMAGINARY)
                String srcFullpathFile = srcFO.getName().getPath();
                String destFullpathFile = destFO.getName().getPath();
                gridftpUtil.thirdPartyFileTransfer(srcClient, srcFullpathFile, destClient, destFullpathFile, append);

            }
        } else if (srcFO.getType().equals(FileType.FOLDER)) {
            if (destFO.getType().equals(FileType.FOLDER)) {
                // copy children of pFrom into pTo folder
                log.debug("gsiftp FOLDER children into FOLDER");
                String srcFullpathFile = srcFO.getName().getPath();
                String destDir = destFO.getName().getPath();
                gridftpUtil.thirdPartyTransferDir2(srcClient, srcFullpathFile, destClient, destDir, append);

            } else {
                throw new IOException("cannot copy a folder to a destination that is not a folder");
            }
        } else {
            throw new IOException("cannot copy from path of type " + srcFO.getType() + " to another path of type " + destFO.getType());
        }

        // Need to manually refresh the content here after the copy because
        // are not using the vfs approach which would normally cause re resolveFile
        GridFtpMlsxFileObject destGridFtpFO = (GridFtpMlsxFileObject) destFO;
        destGridFtpFO.forceGetInfo();

    }

    /**
     * Copy the content of a virtual file from pFrom to another (optionally
     * append the file content to the end of the pTo file) Note. Not all file
     * systems supports the append operation.
     *
     * @param fromFo the file to read from
     * @param toFo the file to append to
     * @param append
     * @throws IOException error during the transfer
     */
    protected static void copyFileToFile(FileObject fromFo, FileObject toFo,
            boolean append) throws IOException {
        if (!fromFo.exists()) {
            throw new IOException("path " + fromFo + " is not found");
        }
        if (!fromFo.getType().equals(FileType.FILE)) {
            throw new IOException("path " + fromFo + " is not a file");
        }

        InputStream xIS = null;
        OutputStream xOS = null;
        try {
            xIS = fromFo.getContent().getInputStream();
            xOS = toFo.getContent().getOutputStream(append);
            byte xBytes[] = new byte[1024];
            int xByteRead = -1;
            int xTotalByteWrite = 0;
            while ((xByteRead = xIS.read(xBytes)) != -1) {
                xOS.write(xBytes, 0, xByteRead);
                xTotalByteWrite = xTotalByteWrite + xByteRead;
            }
            log.info("total byte write " + xTotalByteWrite);
            xOS.flush();
            xOS.flush();
        } catch (Exception xEx) {
            log.error(xEx.getMessage(), xEx);
            throw new IOException(xEx.getMessage());
        } finally {
            try {
                fromFo.getContent().close();
            } catch (Exception xEx) {
            }
            try {
                toFo.getContent().close();
            } catch (Exception xEx) {
            }
            try {
                if (xIS != null) {
                    xIS.close();
                }
            } catch (Exception ex) {
            }
            try {
                if (xOS != null) {
                    xOS.close();
                }
            } catch (Exception ex) {
            }
        }
    }

}

