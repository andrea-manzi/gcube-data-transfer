package randomtest;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFileSystem;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSMetaDataSet;
import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.irods.IRODSFileObject;
import org.apache.commons.vfs2.provider.irods.IRODSFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.irods.IRODSVfsFileSystem;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBFileObject;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.storageresourcebroker.SRBVfsFileSystem;
import org.apache.commons.vfs2.provider.storageresourcebroker.operations.SRBFileOperationProvider;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class TestVFS {
	FileSystemManager fsManager = null;
	
	public TestVFS () throws FileSystemException {
		fsManager = VFS.getManager();
        SRBAccount account = new SRBAccount("ngdata.hpc.jcu.edu.au", 5544, "cima", "cima!2006!", "hpc.jcu.edu.au", "hpc.jcu.edu.au", "", "hpc.jcu.edu.au");
    }
	
	private void listContents ( FileObject fo) throws FileSystemException
	{
		//		 List the children of the Jar file
		FileObject[] children = fo.getChildren();
		FileObject f = null;
		FileContent c = null;
		
		System.out.println( "Children of " + fo.getName() );
		
		for ( int i = 0; i < children.length; i++ )
		{
			f = children[ i ];
			c = f.getContent();
			
			final long size = ( f.getType() == FileType.FILE ) ? c.getSize() : -1;
			final long date = ( f.getType() == FileType.FILE ) ? c.getLastModifiedTime() : -1;
		    System.out.println( f.getName().getPath() 
		    		+ " date:" + date 
		    		+ " Size:" + size  );
		}
	}
	
	void listLocal() throws FileSystemException
	{
		// List root
		FileObject localRoot = fsManager.resolveFile(FileName.SEPARATOR);
		listContents(localRoot);
	}

	void listSFtp() throws FileSystemException
	{
		String user = "vsilva", pwd = "2p2dkdt", share = "";
		String host = "ebony";
			
		String uri = "sftp://" + user + ":" + pwd + "@" + host + "/" + share;

		// List roots
		FileObject file = fsManager.resolveFile(uri);
		listContents(file);
	}

	void listGsiFtp() throws FileSystemException
	{
		String user = "vsilva", pwd = "2p2dkdt", path = "";
		String host = "ebony";
		int port = 2811;
		
		String uri = "gsiftp://" + user + ":" + pwd + "@" + host 
			+ ":" + port + "/" + path;

		// List roots
		FileObject file = fsManager.resolveFile(uri);
		listContents(file);
	}

	void listFtp() throws FileSystemException
	{
		String host = "ftp.kde.org";
			
		String uri = "ftp://" + host + "/";

		// List roots
		FileObject file = fsManager.resolveFile(uri);
		listContents(file);
	}

	void srbList()
	{
		try {
			String user = "cima", pwd = "cima!2006!", path = "/hpc.jcu.edu.au/home/cima.hpc.jcu.edu.au";
			String host = "ngdata.hpc.jcu.edu.au:5544";
				
			String uri = "srb://" + user + ":" + pwd + "@"  + host + path ;
			//String uri = "srb://globus.dev:secret@vm-rhl9:5544/A/home/globus.dev";
			
			// List roots
			long t1 = System.currentTimeMillis();
			
			FileObject file = fsManager.resolveFile(uri);

            //TODO: uncomment for test to work
//			System.err.println("file:" + file + " home=" + file.getFileSystem().getAttribute(SrbFileSystem.HOME_DIRECTORY));
			
			listContents(file);
			
			long t2 = System.currentTimeMillis();
			
			System.out.println("Test complete time (ms)=" + (t2-t1));
			
//			FileObject fo = fsManager.resolveFile("srb://globus.dev:secret@vm-rhl9:5544/A/home/globus.dev/dir1");
//			fo.createFolder();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void srbCopy() 
	{
		try {
			String user = "vsilva.NCC", pwd = "secret", path = "/nccZone/home/vsilva.NCC/";
			String host = "ebony:5544";
			
			String sFile = "foo.txt";
			String dFile = "foo5.txt";
			
			String srcURI = "srb://" + user + ":" + pwd + "@"  + host + path + sFile ;
			String tgtURI = "srb://" + user + ":" + pwd + "@"  + host + path + dFile;
			
			System.out.println("Copying " + srcURI + " to " + tgtURI);
			
			// List roots
			FileObject src = fsManager.resolveFile(srcURI);
			FileObject tgt = fsManager.resolveFile(tgtURI);
			
			FileUtil.copyContent(src, tgt);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void directSRBList() throws IOException, FileNotFoundException
	{
		//uses the ~/.srb/MdasEnv user info file
    	SRBAccount acct = new SRBAccount();

    	edu.sdsc.grid.io.srb.SRBFileSystem fs = 
    		(edu.sdsc.grid.io.srb.SRBFileSystem) FileFactory.newFileSystem( acct );
    	
    	SRBFile f = (SRBFile)FileFactory.newFile(fs, acct.getHomeDirectory());
    		
		System.out.println("MCAT Zone:" + acct.getMcatZone() + " Dom:" + acct.getDomainName() );
		System.out.println("Home:" + acct.getHomeDirectory() + " Def res:" + acct.getDefaultStorageResource());
		
		String[] children = f.list();
		
		for (int i = 0; i < children.length; i++) {
			System.out.println(children[i]);
		}

	}

	private void uriSRBTest() throws URISyntaxException, IOException, FileNotFoundException
 	{
	
		String uri = "srb://vsilva.NCC:secret@ebony:5544/nccZone/home/vsilva.NCC";
		SRBFile f = (SRBFile)FileFactory.newFile(new URI(uri));
		SRBAccount acct = (SRBAccount)f.getFileSystem().getAccount();

		System.out.println("MCAT Zone:" + acct.getMcatZone() + " Dom:" + acct.getDomainName() );
		System.out.println("Home:" + acct.getHomeDirectory() + " Def res:" + acct.getDefaultStorageResource());
		
		String[] children = f.list();
		
		for (int i = 0; i < children.length; i++) {
			System.out.println(children[i]);
		}

 	}
	
	void listHttp ()
	{
		try {
			String uri = "http://cvs.apache.org/builds/jakarta-commons/nightly/commons-vfs/";
			
			listContents(fsManager.resolveFile(uri));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void dummy () {
		try {
			FileObject fo1 = fsManager.resolveFile("file:///c:/Documents and Settings");
			FileObject fo2 = fsManager.resolveFile("file:///c:/Documents and Settings/");
			System.out.println("fo1=" + fo1 + " fo2=" + fo2 + " equal=" + fo1.equals(fo2));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

    private static String getURIString(SRBAccount srbAccount) {
        return "srb://" + srbAccount.getHost() + ":" + srbAccount.getPort();
    }

    private static String getURIString(IRODSAccount irodsAccount){
        return "irods://" + irodsAccount.getHost() + ":" + irodsAccount.getPort();
    }

    public static void main(String[] args) throws FileSystemException, GlobusCredentialException, GSSException
    {
        GlobusCredential gCert = GlobusCredential.getDefaultCredential();

        System.out.println("id: " + gCert.getIdentity());
        System.out.println("getting default credentials");

        GSSCredential cert = new GlobusGSSCredentialImpl(gCert, GSSCredential.INITIATE_AND_ACCEPT);
        //SRBAccount srbAccount = new SRBAccount("srbdev.sf.utas.edu.au",
        //                    5544,
        //                    cert);

        IRODSAccount irodsAccount = new IRODSAccount("srbdev.vpac.org", 1247,
                                                    "", "", "",
                                                    "srbdev.vpac.org", "datafabric.srbdev.vpac.org");


        irodsAccount.setGSSCredential(cert);

        //FileSystemOptions irodsOpts = new FileSystemOptions();
        //IRODSFileSystemConfigBuilder irodsBuilder = IRODSFileSystemConfigBuilder.getInstance();
        //irodsBuilder.setAccount(irodsAccount, irodsOpts);
        //IRODSVfsFileSystem vfsFileSystem = (IRODSVfsFileSystem) VFS.getManager().resolveFile(getURIString(irodsAccount), irodsOpts).getFileSystem();
        //IRODSFileSystem irodsFs = (IRODSFileSystem) vfsFileSystem.getAttribute(IRODSVfsFileSystem.FILESYSTEM);
        try
        {
            IRODSFileSystem irodsFS = new IRODSFileSystem(irodsAccount);
            System.out.println("after constructor - about to query with GSI");

            MetaDataRecordList[] recordList = null;

            try
            {
                recordList = irodsFS
                .query(
                        new MetaDataCondition[] {
                                MetaDataSet
                                .newCondition(
                                        IRODSMetaDataSet.USER_DN,
                                        MetaDataCondition.EQUAL,
                                        "/C=AU/O=APACGrid/OU=TPAC/CN=Pauline Mak")
                        },
                        new MetaDataSelect[] { MetaDataSet
                                .newSelection(IRODSMetaDataSet.USER_NAME) });
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if(recordList == null)
            {
                System.out.println("getiRODSUsernameByDN: no result");
            }
            else
            {
                System.out.println("getiRODSUsernameByDN: " + recordList.length);
            }

            if(recordList != null&&recordList.length>0)
            {
                System.out.println("result: " + (String)recordList[0].getValue(IRODSMetaDataSet.USER_NAME));
            }

            System.out.println("account 1 info: " + irodsAccount.getUserName());

            IRODSAccount irodsAccount2 = new IRODSAccount("srbdev.vpac.org", 1247,
                                                    "paulinemak1", "", "/srbdev.vpac.org/home/paulinemak1",
                                                    "srbdev.vpac.org", "datafabric.srbdev.vpac.org");

            irodsAccount2.setGSSCredential(cert);

            System.out.println("acount 2");
            
            FileSystemOptions irodsOpts = new FileSystemOptions();
            IRODSFileSystemConfigBuilder irodsBuilder = IRODSFileSystemConfigBuilder.getInstance();
            irodsBuilder.setAccount(irodsAccount2, irodsOpts);
            
            System.out.println("Account 2 URI: " + getURIString(irodsAccount2) + " " + irodsAccount2.getUserName());


            IRODSFileSystem irodsFS2 = (IRODSFileSystem)(VFS.getManager().
                    resolveFile(getURIString(irodsAccount2), irodsOpts)
                    .getFileSystem().getAttribute(IRODSVfsFileSystem.FILESYSTEM));

            System.out.println("resolved FS " + irodsFS2);

            String irodsUriString = new IRODSFile(irodsFS2, "/srbdev.vpac.org/home/paulinemak1/errors.txt").toURI().toString();

            System.out.println("irodsUriString " + irodsUriString);

             //SRB uri appends these characters which screw with the vfs module
            irodsUriString = irodsUriString.replace("?#", "");

            FileObject irodsFO = VFS.getManager().resolveFile(irodsUriString, irodsOpts);

            try
            {
                IRODSFileObject irodsFile = ((IRODSFileObject)(irodsFO));
                System.out.println("irodsFile: " + irodsFile.getURL().toString());
                FileContent content = irodsFile.getContent();
                System.out.println("about to get outputstream");
                BufferedOutputStream fileOut = new BufferedOutputStream(content.getOutputStream());

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            //FileObject testFolder = irodsFO.resolveFile("testFolder2/asdfasdf");

            //System.out.println("testFolder: " + testFolder.getName());
            //testFolder.createFolder();


            //FileSystemManager fsManager = VFS.getManager();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        SRBAccount srbAccount = new SRBAccount("srbdev.sf.utas.edu.au",
                5544,
                "pmak",
                "adf",
                "/srbdev.sf.utas.edu.au/home/pmak.srbdev.sf.utas.edu.au",
                "srbdev.sf.utas.edu.au",
                "datafabric.srbdev.sf.utas.edu.au",
                "srbdev.sf.utas.edu.au");

        srbAccount.setDefaultStorageResource("datafabric.srbdev.sf.utas.edu.au");
        FileSystemOptions opts = new FileSystemOptions();
        SRBFileSystemConfigBuilder configurations = SRBFileSystemConfigBuilder.getInstance();
        configurations.setAccount(srbAccount, opts);

        SRBFileSystem srbFs = (SRBFileSystem) VFS.getManager().resolveFile(getURIString(srbAccount), opts)
                .getFileSystem().getAttribute(SRBVfsFileSystem.FILESYSTEM);

        String uriString = new SRBFile(srbFs, "/srbdev.sf.utas.edu.au/home/pmak.srbdev.sf.utas.edu.au/TestDir").toURI().toString();


        //SRB uri appends these characters which screw with the vfs module
        uriString = uriString.replace("?#", "");

        FileObject fs = VFS.getManager().resolveFile(uriString, opts);

        FileSystemManager fsManager = VFS.getManager();
        SRBFileOperationProvider opsProvider = new SRBFileOperationProvider();
        fsManager.addOperationProvider("srb", opsProvider);

        SRBFileObject srbFo = (SRBFileObject)fs;

        FileObject t = fs.resolveFile("TestDir/TestDir");
        t.createFolder();

        System.out.println("srbFo" + srbFo);


        /*try
        {

            //srbFs.srbModifyUser(SRBFile.MDAS_CATALOG, "paulinemak@srbdev.sf.utas.edu.au",
            //        "myGroup", SRBMetaDataSet.U_INSERT_GROUP);

            srbFs.srbModifyUser(SRBFile.MDAS_CATALOG, "paulinemak@srbdev.sf.utas.edu.au",
                   "myGroup", SRBMetaDataSet.U_INSERT_GROUP);
        }
        catch(Exception e)
        {
            System.err.println("error while trying to get permssion list of a file: " + e.toString());
            e.printStackTrace();
        }*/
        }
                        
    

    /*public static void main(String[] args) throws FileSystemException, GlobusCredentialException, GSSException {
		GlobusCredential gCert = GlobusCredential.getDefaultCredential();
        GlobusGSSCredentialImpl cert = new GlobusGSSCredentialImpl(gCert, GSSCredential.INITIATE_AND_ACCEPT);
        SRBAccount srbAccount = new SRBAccount("srb.ivec.org",
                            5544,
                            cert);
        srbAccount.setDefaultStorageResource("srb.ivec.org");

        FileSystemOptions opts = new FileSystemOptions();
        SRBFileSystemConfigBuilder configurations = SRBFileSystemConfigBuilder.getInstance();
        configurations.setAccount(srbAccount, opts);
        //SRBFileSystem srbFs = (SRBFileSystem) VFS.getManager().resolveFile(getURIString(srbAccount), opts).getFileSystem().getAttribute(SRBVfsFileSystem.SRB_FILESYSTEM);
        SRBFileSystem srbFs = (SRBFileSystem) VFS.getManager().resolveFile(getURIString(srbAccount), opts).getFileSystem().getAttribute(SrbFileSystem.SRB_FILESYSTEM);

        String uriString = new SRBFile(srbFs, srbFs.getHomeDirectory()).toURI().toString();

        //SRB uri appends these characters which screw with the vfs module
        uriString = uriString.replace("?#", "");
        System.out.println(uriString);
        
        FileObject fs = VFS.getManager().resolveFile(uriString, opts);

        FileObject[] children = fs.getChildren();

        long prevTime = System.currentTimeMillis();

        for(FileObject child : children) {
            if(child.getType() == FileType.FOLDER) {
                FileObject[] moreChildren = child.getChildren();
                for(FileObject newChild : moreChildren)
                    if(newChild.getType() == FileType.FILE)
                        System.out.println("    " + newChild.getName() + " " + newChild.getContent().getSize() + " " + newChild.getContent().getLastModifiedTime());
            }
            if(child.getType() == FileType.FILE)
                System.out.println(child.getName() + " " + child.getContent().getSize() + " " + child.getContent().getLastModifiedTime());
            System.out.println(child.isReadable());
        }
        System.out.println("total time " + (System.currentTimeMillis() - prevTime));

        System.out.println("");
        System.out.println("starting again");
        System.out.println("");

        fs = VFS.getManager().resolveFile(uriString, opts);

        children = fs.getChildren();

        prevTime = System.currentTimeMillis();

        for(FileObject child : children) {
            if(child.getType() == FileType.FOLDER) {
                FileObject[] moreChildren = child.getChildren();
                for(FileObject newChild : moreChildren)
                    if(newChild.getType() == FileType.FILE)
                        System.out.println("    " + newChild.getName() + " " + newChild.getContent().getSize() + " " + newChild.getContent().getLastModifiedTime());
            }
            if(child.getType() == FileType.FILE)
                System.out.println(child.getName() + " " + child.getContent().getSize() + " " + child.getContent().getLastModifiedTime());
            System.out.println(child.isReadable());
        }
        System.out.println("total time " + (System.currentTimeMillis() - prevTime));
    } */

}
