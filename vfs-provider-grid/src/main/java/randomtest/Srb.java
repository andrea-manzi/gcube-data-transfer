package randomtest;

public class Srb 
{
	/*SRBFileSystem srbFileSystem ;

	// SRB File Attribute constants
    public static final String FILE_NAME = SRBMetaDataSet.FILE_NAME;
    public static final String SIZE = SRBMetaDataSet.SIZE;
	public static final String USER_DOMAIN = SRBMetaDataSet.USER_DOMAIN;
	public static final String OWNER_DOMAIN = SRBMetaDataSet.OWNER_DOMAIN;
	public static final String FILE_COMMENTS = SRBMetaDataSet.FILE_COMMENTS;
	public static final String FILE_TYPE_NAME = SRBMetaDataSet.FILE_TYPE_NAME;
	public static final String USER_NAME = SRBMetaDataSet.USER_NAME;
	public static final String USER_GROUP_NAME = SRBMetaDataSet.USER_GROUP_NAME;
	public static final String FILE_LAST_ACCESS_TIMESTAMP = SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP;
	public static final String RESOURCE_NAME = SRBMetaDataSet.RESOURCE_NAME;
	
	public randomtest.Srb() throws Exception
	{
		srbFileSystem = (SRBFileSystem)FileFactory.newFileSystem(
                new SRBAccount("ngdata.hpc.jcu.edu.au", 5544, "cima", "cima!2006!", "/hpc.jcu.edu.au/home/cima.hpc.jcu.edu.au", "hpc.jcu.edu.au", "")
        );
		System.out.println("file system=" + srbFileSystem);
	}
	
	 *//*
     * Get an SRB Attribute value from a query record
     *//*
    private Object getSrbAttribute (MetaDataRecordList record, String key) 
    {
    	return record.getValue(record.getFieldIndex(key));
    }
	void getFiles (GeneralFile file) throws Exception
	{
		final String[] selectFieldNames = {
				SRBMetaDataSet.FILE_NAME,
				SRBMetaDataSet.FILE_COMMENTS,
				SRBMetaDataSet.FILE_TYPE_NAME,
				SRBMetaDataSet.SIZE,
				SRBMetaDataSet.USER_DOMAIN,
				SRBMetaDataSet.OWNER_DOMAIN,
				SRBMetaDataSet.USER_NAME,
				SRBMetaDataSet.USER_GROUP_NAME,
				SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP,
				SRBMetaDataSet.RESOURCE_NAME
			};
			
			MetaDataSelect selects[] = MetaDataSet.newSelection( selectFieldNames );
    		*//**
    		 * The metadata records list for each file (directory, or other value
    		 * the query selected for) is stored in this array.
    		 *//*
    		MetaDataRecordList[] results = file.query(selects);

    		if ( results != null ) {
    			for (int i = 0; i < results.length; i++) {
    				final String name	= (String)getSrbAttribute(results[i], FILE_NAME);
        			final long size		= Long.parseLong((String)getSrbAttribute(results[i], SIZE));
        			final String sDate 	= (String)getSrbAttribute(results[i], FILE_LAST_ACCESS_TIMESTAMP);
        			final String type 	= (String)getSrbAttribute(results[i], FILE_TYPE_NAME);

        			System.out.println("queryChildrenInfo file[" + i + "] for path " + file.getPath() 
							+ " F Name=" + name + " Size=" + size 
							+ " Date: " + sDate + " Type:" + type);
				}
    		}
    		
	}
	
	void getDirectories (GeneralFile file) throws Exception
	{
		MetaDataCondition conditions[] = new MetaDataCondition[1];
		MetaDataSelect selects[] = { MetaDataSet.newSelection( SRBMetaDataSet. DIRECTORY_NAME ) };
		
		System.out.println("file=" + file + " is dir=" + file.isDirectory());
		
		String path = (file.isDirectory()) ? file.getAbsolutePath() : file.getParent();
		
		conditions[0] = MetaDataSet.newCondition(
				SRBMetaDataSet.PARENT_DIRECTORY_NAME, MetaDataCondition.EQUAL, path );
		
		MetaDataRecordList[]results = srbFileSystem.query(conditions, selects);

		System.out.println("getDirectories for Path=" + path + " results=" + results);
		
		if ( results != null ) {
			for (int i = 0; i < results.length; i++) {
				System.out.println("Sub Folder=" + getSrbAttribute(results[i], SRBMetaDataSet.DIRECTORY_NAME)
						+ " in path " + file.getPath());
			}
		}

	}
	
	void mkdir()
	{
		//String uri = "srb://globus.dev:secret@vm-rhl9:5544/A/home/globus.dev/dir1";
		SRBFile f = (SRBFile)FileFactory.newFile(getFileSystem(),"/A/home/globus.dev/dir1");
		System.out.println("file=" + f + " exits=" + f.exists() + " is dir=" + f.isDirectory());
		f.mkdir();
	}
	
	*//**
	 * 
	 * @param file
	 * @return
	 *//*
*//*
	public String[] list( GeneralFile file )
	{
		MetaDataCondition conditions[] = new MetaDataCondition[1];
		MetaDataSelect selects[] = {
			MetaDataSet.newSelection( SRBMetaDataSet.FILE_NAME ) };
		MetaDataRecordList[] rl1 = null;
		MetaDataRecordList[] rl2 = null;
		MetaDataRecordList[] temp = null;
		Vector list = null;
		String path = null;


		try {
			//Have to do two queries, one for files and one for directories.
			if (file.isDirectory()) {
				path = file.getAbsolutePath();
			}
			else {
				path = file.getParent();
			}

			//get all the files
			conditions[0] = MetaDataSet.newCondition(
				SRBMetaDataSet.DIRECTORY_NAME, MetaDataCondition.EQUAL, path );
			
			rl1 = srbFileSystem.query(
				conditions, selects); //, SRBFileSystem.DEFAULT_RECORDS_WANTED );
			
			//get all the sub-directories
			selects[0] = MetaDataSet.newSelection( SRBMetaDataSet.DIRECTORY_NAME );
			conditions[0] = MetaDataSet.newCondition(
				SRBMetaDataSet.PARENT_DIRECTORY_NAME, MetaDataCondition.EQUAL, path );

			rl2 = srbFileSystem.query(
				conditions, selects); //, SRBFileSystem.DEFAULT_RECORDS_WANTED );

			//change to relative path
			if (rl2 != null) {
				String absolutePath = null;
				String relativePath = null;
				for (int i=0;i<rl2.length;i++) {
					//only one record per rl
					absolutePath = rl2[i].getStringValue(0);
					relativePath = absolutePath.substring(
						absolutePath.lastIndexOf( "/" )+1 );
					rl2[i].setValue( 0, relativePath );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
			return null;
		}


		if (( rl1 != null ) && (rl2 != null)) {
			//length of previous query + (new query - table and attribute names)
			temp = new SRBMetaDataRecordList[rl1.length+rl2.length];
			//copy files
			System.arraycopy( rl1, 0, temp, 0, rl1.length );
			System.arraycopy( rl2, 0, temp, rl1.length, rl2.length );
		}
		else if (rl1 != null) {
			temp = rl1;
		}
		else if (rl2 != null) {
			temp = rl2;
		}
		else {
			return new String[0];
		}

		list = new Vector();
		for (int i=0;i<temp.length;i++) {
			if (temp[i].getStringValue(0) != null) {
				//only one record per rl
				list.add(temp[i].getStringValue(0));
			}
		}

		return (String[]) list.toArray(new String[0]);
	}
*//*
	
	*//*
	 * 
	 *//*
	public GeneralFileSystem getFileSystem ()
	{
		return srbFileSystem;
	}
	
	*//**
	 * @param args
	 *//*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			randomtest.Srb s = new randomtest.Srb();

			
			//GeneralFile file = FileFactory.newFile(s.getFileSystem(),"/A/home/globus.dev");
			//s.getFiles(file);
			//s.getDirectories(file);
			s.mkdir();
			
//			String[] files = s.list(file);
//			for (int i = 0; i < files.length; i++) {
//				System.out.println(files[i]);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
}
