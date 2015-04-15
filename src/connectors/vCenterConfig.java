package connectors;


@SuppressWarnings("static-access")
public class vCenterConfig {
   
	private static String adminURL="https://130.65.132.19/sdk";
    private static String adminUser="student@vsphere.local";
    private static String URL="https://130.65.132.110/sdk";
    private static String uName="administrator";

    private static String adminPass="12!@qwQW";
    private static String pWord="12!@qwQW";

    private static String hostUser="root";
    
    public static String getHostUser() {
		return hostUser;
	}

	public String getURL() {
        return URL;
    }

	public void setURL(String URL) {
        this.URL = URL;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getpWord() {
        return pWord;
    }

    public void setpWord(String pWord) {
        this.pWord = pWord;
    }

    public static String getAdminUser() {
    	return adminUser;
    }

    public static String getAdminURL() {
    	return adminURL;
    }

    public static String getAdminPass() { 
    	return adminPass; 
    }

}
