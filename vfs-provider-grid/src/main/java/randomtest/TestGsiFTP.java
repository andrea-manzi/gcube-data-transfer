package randomtest;

public class TestGsiFTP 
{
	/*GridFTPClient client;
	String host;
	int port =2811;
	
	public randomtest.TestGsiFTP () throws IOException, ServerException, ClientException
	{
		host = "ebony";
		client = new GridFTPClient(host, port);
		
		// login w/ def creds
		client.authenticate(null);
		
	}
	
	public void testList(String dir) throws Exception
	{
		System.out.println("Current dir:" + client.getCurrentDir() );

		*//** required to perform multiple requets **//*
        client.setLocalPassive();
        client.setActive();
		
		Vector v = client.mlsd(dir);
		
//		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
//		String date = null;
		
		for (int i = 0; i < v.size(); i++) {
			MlsxEntry e = (MlsxEntry)v.get(i);
			//date = e.getDate() + " " + fi.getTime();
			
			//System.out.println(fi.getName() + " Date=" + sdf.parse(date) );
			System.out.println(e);
			
		}

        
//		v = client.mlsd(dir);
//		for (int i = 0; i < v.size(); i++) {
//			MlsxEntry e = (MlsxEntry)v.get(i);
//			//date = e.getDate() + " " + fi.getTime();
//			
//			//System.out.println(fi.getName() + " Date=" + sdf.parse(date) );
//			System.out.println(e);
//			
//		}
		
	}
	
	*//**
	 * @param args
	 *//*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			randomtest.TestGsiFTP t = new randomtest.TestGsiFTP();
			t.testList("/");
			t.testList("/tmp");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
