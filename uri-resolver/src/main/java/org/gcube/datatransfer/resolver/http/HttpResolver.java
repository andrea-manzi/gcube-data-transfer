package org.gcube.datatransfer.resolver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.gcube.contentmanager.storageclient.model.protocol.smp.Handler;
import org.gcube.contentmanager.storageclient.model.protocol.smp.SMPURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 * @author Andrea Manzi(CERN)
 *
 */
public class HttpResolver extends HttpServlet {
	
	String uri =null;
	String fileName =null;
	String contentType =null;

	private static final long serialVersionUID = 1L;

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(HttpResolver.class);

	public void init(ServletConfig conf) throws ServletException {
		Handler.activateProtocol();
		super.init(conf);

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
	

		logger.info("The http session id is: " + request.getSession().getId());
		
		logger.debug("Original URI = "+ uri);

		
		uri = request.getParameter("smp-uri");

		if (uri == null || uri.equals("")) {
			logger.debug("URI not found");
			response.sendError(404);
			return;
		}
		
		fileName  = request.getParameter("fileName");
		
		
		if (fileName == null || fileName.equals("")) {
			logger.debug("fileName not found");
			fileName = null;
		}

		contentType  = request.getParameter("contentType");
		
		if (contentType == null || contentType.equals("")) {
			logger.debug("contentType not found");
			contentType = null;
		}
		
		//we should not unescape the filename with spaces
		
		int index= uri.indexOf("?");
		if ( index!= -1)
		{
			String firsPart = uri.substring(0, index);
			String secondPart=  uri.substring( index+1);
			secondPart.replace(" ","+");//the char + is removed when the servlet is doing unescaping of the query paramenters, we just put it back
			uri= firsPart+"?"+secondPart;
		}
		else uri = uri.replace(" ","+");//the char + is removed when the servlet is doing unescaping of the query paramenters, we just put it back

		
		logger.debug("URI = "+ uri);

		try {


			OutputStream out = response.getOutputStream();
			
			if (fileName != null)
				response.addHeader("content-disposition", "attachment; filename=" +fileName);
			else
				response.addHeader("content-disposition", "attachment; filename=fromStorageManager");
			
			if (contentType!= null)
				response.setContentType(contentType);
			else	
				response.setContentType("unknown/unknown");
			
			

			URL url = new URL(null, uri, new URLStreamHandler() {
				
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					return new SMPURLConnection(u);
				}
			});
		
			URLConnection uc = null;
			
			InputStream in = null;
			
			try {
				uc = ( URLConnection ) url.openConnection ( );
				in = uc.getInputStream();
			}
			catch(Exception e){
				response.sendError(404);
				logger.error("Exception:", e);
				return;
			}


			IOUtils.copy(in, out);
		
			out.flush();
			out.close();
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
			response.sendError(404);
			return;
		}

	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		doGet(request,response);
	}
	

}