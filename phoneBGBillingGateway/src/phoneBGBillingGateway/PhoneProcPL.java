package phoneBGBillingGateway;

public class PhoneProcPL {
	private static final String base_path = "/opt/bgbilling/script/meraconv/LOG";
	private static final String OLD_PATH = "/opt/bgbilling/script/meraconv/LOG/csv_old";
	private static final String SOURCE_LOG = "/home/mera"; // !!!!!!!
	private static final String store_path = "/opt/bgbilling/script/meraconv/LOG/csv_store";
	private static final String destlog_path = OLD_PATH + "/source";
	private final Logger logger = Logger.getLogger( PhoneProcPL.class );
	private ArrayList<String> linesList = new ArrayList<String>();
	private	String[] products[];
	
	
	public final void execute( Setup setup, ConnectionSet connectionSet )		throws Exception 	{
		
//		prepareFiles();
		prepareFilesOld();
		fillRawData();
		writeRawLog();
		
    }
	
	private void fillRawData() {
	    try {
	    	linesList.clear();
    	    for (File myFile : new File(OLD_PATH).listFiles()) {
                if (myFile.exists() && myFile.isFile())  {
					logger.info("Работаю с файлом: " + myFile.toString());
					BufferedReader reader = new BufferedReader(new FileReader(myFile.toString()));
					String line;    	    
   					while ((line = reader.readLine()) != null) {			linesList.add(line);   					}
                }
            }
            products = new String[linesList.size()][9];
            for (int i=0; i< linesList.size(); i++) { 
            	products[i] = linesList.get(i).split("\\|");

				// конверт времени            	
            	String[] time = products[i][1].split("\\ ");
            	products[i][1] = time[3]+"."+getMonth(time[2])+"."+time[4]+" "+time[0];
            	
            	// конверт длительности
            	products[i][8] =  String.valueOf(Integer.parseInt(products[i][8])/1000); //Integer.parseInt(products[i][8]/1000)
            	
            	if (products[i][2].length()>6){
            	 if (products[i][2].length() == 7) { products[i][2] = "7846" + products[i][2]; }
             	 if (products[i][2].length() == 10) { products[i][2] = "7" + products[i][2]; }
             	 products[i][2] = fix8(products[i][2]);

             	 
            	}
            	if (products[i][3].length()>6) {
            	 if (products[i][3].length() == 7) { products[i][3] = "7846" + products[i][3]; }
             	 if (products[i][3].length() == 10) { products[i][3] = "7" + products[i][3]; }
             	 products[i][3] = fix8(products[i][3]);            	 
              	 if (products[i][3].substring(0,2).equals("810")) { products[i][2] = products[i][3].substring(0,3); } // возможна ошибка индекса.
            	}
            	
            	
            	if (products[i][3].length()==2) {
            	 if (products[i][2].length() == 7) { products[i][2] = "7846" + products[i][2]; }
             	 if (products[i][2].length() == 10) { products[i][2] = "7" + products[i][2]; }
             	 products[i][2] = fix8(products[i][2]);
            	}
            	
            	System.out.println(products[i][1]);
//            	System.out.println(products[i][3]);
            }
       }
	   catch (Exception e) { e.printStackTrace(); }		       
    }
    
    
   private void writeRawLog() {
   
   		for (int i = 0; i< products[i].length; i++) {
	   		String line = products[i][1]+"\t"+products[i][8]+"\t"+products[i][2]+"\t"+products[i][2]+"\t"+products[i][3]+"\t"+products[i][3]+"\t"+
   						products[i][6]+"\t"+products[i][7] +"\t0\t"+products[i][8]+"\t0\n";
//           	System.out.println(line);
       try(FileWriter writer = new FileWriter("/opt/bgbilling/tmp/asd", false))        {
            writer.write(line);
            writer.flush();
        }
        catch(IOException ex){   System.out.println(ex.getMessage());     }
    	}
   }
    
    
    private String fix8(String str) {
    	if (str.substring(0,1).equals("8") && (str.length() == 11) && (!str.substring(0,2).equals("810")) ) {
    		return "7" + str.substring(1,10);
    	}
    	else  { return str; }
    }
	


private String getMonth(String str1)
{
	if  (str1.equals("Jan")) {      return  "01"; }
    else if  (str1.equals("Feb")) {      return  "02"; }
    else if  (str1.equals("Mar")) {      return  "03"; }
    else if  (str1.equals("Apr")) {      return  "04"; }
    else if  (str1.equals("May")) {      return  "05"; }
    else if  (str1.equals("Jun")) {      return  "06"; }
    else if  (str1.equals("Jul")) {      return  "07"; }
    else if  (str1.equals("Aug")) {      return  "08"; }
    else if  (str1.equals("Sep")) {      return  "09"; }
    else if  (str1.equals("Oct")) {      return  "10"; }
    else if  (str1.equals("Nov")) {      return  "11"; }
    else if  (str1.equals("Dec")) {      return  "12"; }
    else return null;
}





	
	private void prepareFilesOld() {
		try {	galkovBGCRM.executeExternalScript("/opt/bgbilling/script/phoneSystem_1.sh");    		}
		catch ( InterruptedException | IOException e) { e.printStackTrace(); }
	}
		
	private void prepareFiles() {
		try {
			for (File myFile : new File(SOURCE_LOG).listFiles()) {
		
				String path = OLD_PATH + "/" + myFile.getName();
				logger.info("перемещаю :" + myFile.getName() + " сюда:" + path);
	    		myFile.renameTo(new File(path));
    		}
    	}
    	catch (Exception e) { e.printStackTrace(); }			
	}
	
}
