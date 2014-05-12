package org.gcube.datatransfer.portlets.sm.user.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.gcube.datatransfer.portlets.sm.user.server.utils.Constants;
import org.gcube.datatransfer.portlets.sm.user.server.utils.TransferUtils;
/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class FileUpload extends HttpServlet{

	private static final long serialVersionUID = 7568943356533922462L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();
		
		try{
			FileItemIterator iter = upload.getItemIterator(request);
			String username="N_A";
			String filename="N_A";
			InputStream streamIn=null;

			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String currentFieldName = item.getFieldName();	

				if(item.isFormField()){
					System.out.println("FileUpload - doPost - formField="+currentFieldName);
					InputStream currentStreamIn=item.openStream();

					if(currentFieldName.compareTo("username")==0){
						username=Streams.asString(currentStreamIn);
						System.out.println("FileUpload - doPost - username="+username);
					}
					else if(currentFieldName.compareTo("filename")==0){
						filename=Streams.asString(currentStreamIn);
						filename=filename.replaceAll(" ", "_");		
						//in case of an encoded name ... 
						filename=TransferUtils.decodeSomeNameCompletelly(filename); // decoded as many times as possible
						if(filename==null)filename="N_A";
						System.out.println("FileUpload - doPost - filename="+filename);
					}
					currentStreamIn.close();
				}
				else {
					System.out.println("FileUpload - doPost - uploadField="+currentFieldName);
					if (currentFieldName.compareTo("uploadedfile")==0){
						streamIn = item.openStream();
						
						if(username==null)username="N_A";
						if(filename==null)filename="N_A";

						if(streamIn==null){
							throw new Exception("FileUpload - doPost - Exception - streamIn==null");
						}

						// Process the input stream               
						/*ByteArrayOutputStream out = new ByteArrayOutputStream();
						int len;
						byte[] buffer = new byte[8192];
						while ((len = streamIn.read(buffer, 0, buffer.length)) != -1) {
							out.write(buffer, 0, len);
						}

						int maxFileSize = Constants.maxSizeForFileUpload; //500 megs max 
						if (out.size() > maxFileSize) { 
							throw new RuntimeException("File is > than " + maxFileSize);
						} */
						
						String path="/tmp/storagemanager-portlet/"+username+"/";
						File rootFolders=new File(path);
						rootFolders.mkdirs();

						File tmpFile = new File(path+filename);
						tmpFile.createNewFile();

						System.out.println("FileUpload - doPost - created structure");

						OutputStream streamOut = new FileOutputStream(tmpFile);
						System.out.println("FileUpload - doPost - get the output stream");

						IOUtils.copy(streamIn, streamOut);    
						System.out.println("FileUpload - doPost - file successfully uploaded to "+path+filename);
						
						streamIn.close();
						streamOut.close();
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
}