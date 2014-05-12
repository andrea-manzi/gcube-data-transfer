package org.gcube.datatransfer.portlets.sm.user.server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.SmpFileProvider;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;


/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class TransferUtils {

	public static FileSystemOptions createDefaultOptions(String URI)
			throws FileSystemException {
		// Create SFTP options
		FileSystemOptions opts = new FileSystemOptions();

		int timeout = Constants.defaultTimeOut;
		//check the URL type
		if (URI.startsWith("ftp://")){

			// Root directory set to user home
			FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);

			// Timeout is count by Milliseconds
			FtpFileSystemConfigBuilder.getInstance().setSoTimeout(opts, timeout);

			FtpFileSystemConfigBuilder.getInstance().setDataTimeout(opts,timeout);
			return opts;
		} else if (URI.startsWith("sftp://")){
			// Root directory set to user home
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);

			// Timeout is count by Milliseconds
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, timeout);
			return opts;
		} else if (URI.startsWith("s3://")){

			//com.scoyo.commons.vfs.S3Util.initS3Provider(ServiceContext.getContext().getAwsKeyID(),ServiceContext.getContext().getAwsKey());
		}else if (URI.startsWith("http://") || URI.startsWith("http://")) {
			// Root directory set to user home
			HttpFileSystemConfBuilderPatched.getInstance().setTimeout(opts, timeout);
			//HttpsFileSystemConfBuilderPatched.getInstance().s
			return opts;
		}
		else if(URI.startsWith("smp://") ){
			return opts;
		}
		return opts;
	}

	public static FileObject prepareFileObject(String URI)
			throws FileSystemException {
		System.out.println("prepareFileObject - "+URI);
		if(URI.startsWith("smp://")){
			DefaultFileSystemManager defaultmanag= new DefaultFileSystemManager();
			defaultmanag.addProvider("smp", new SmpFileProvider());
			defaultmanag.setDefaultProvider(new UrlFileProvider());
			defaultmanag.init();
			
			return defaultmanag.resolveFile(URI,createDefaultOptions(URI));
		}
		return VFS.getManager().resolveFile(URI,createDefaultOptions(URI));

	}

	public static String encodeSomeName(String tmp){
		String encodedValue=null;
		try {		    
			encodedValue= URLEncoder.encode(tmp, "UTF-8");
		} catch (UnsupportedEncodingException uee) { }
		return encodedValue;
	}

	public static String decodeSomeNameCompletelly(String tmp){
		if(tmp==null)return null;
		String decodedValue=tmp;
		boolean flag=true;

		while(flag){
			try {
				decodedValue = URLDecoder.decode(tmp, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				return null;
			}

			if(decodedValue.compareTo(tmp)==0)flag=false;
			else{
				tmp=decodedValue;
			}		
		}
		return decodedValue;
	}
	
	public static String guessMimeType(String rpath){
		String mimeType = URLConnection.guessContentTypeFromName(rpath);
		if(mimeType==null)mimeType="unknown";
		return mimeType;
	}
}
