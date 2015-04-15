package connectors;

	import com.vmware.vim25.mo.ServiceInstance;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

	public class Connectors {

	    public ServiceInstance sInstance;
	    public ServiceInstance sAdminInstance;

	    public ServiceInstance getConn()throws MalformedURLException ,RemoteException{
	        vCenterConfig conn=new vCenterConfig();
	        String urlStr = conn.getURL();
	        String username = conn.getuName();
	        String password = conn.getpWord();
	        sInstance = new ServiceInstance(new URL(urlStr), username, password, true);
	        return sInstance;
	    }
	    
	    @SuppressWarnings("static-access")
	    public ServiceInstance getAdminConn()throws MalformedURLException ,RemoteException{
	        vCenterConfig connAdmin=new vCenterConfig();
			String adminUrlStr = connAdmin.getAdminURL();
	        String adminusername = connAdmin.getAdminUser();
	        String adminpassword = connAdmin.getAdminPass();
	        sAdminInstance = new ServiceInstance(new URL(adminUrlStr), adminusername, adminpassword, true);
	        return sAdminInstance;
	    }
	    
	    
}