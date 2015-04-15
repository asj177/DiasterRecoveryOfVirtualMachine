
package view;

import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

import connectors.Connectors;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;

public class PerformanceStats extends Thread {
	ServiceInstance si=null;
	public PerformanceStats(ServiceInstance si) throws MalformedURLException, RemoteException {
    	Connectors conn=new Connectors();
        ServiceInstance sInstance;
			sInstance = conn.getConn();
			this.si = sInstance;
    }

    public void run() {
        while (true) {
            try {
                Folder rootFolder = si.getRootFolder();
                String name = rootFolder.getName();
                ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
                if (mes == null || mes.length == 0) 
                {
                    System.out.println("No Hosted VM's found in your Vcenter...");
                    return;
                }
                Thread.sleep(1000);//sleep for 2 minutes
                for (ManagedEntity me : mes) 
                {
                	VirtualMachine vm = (VirtualMachine) me;
                	if(!vm.getConfig().template){
		                    VirtualMachineConfigInfo vminfo = vm.getConfig();
		                    /*VirtualMachineCapability vmc = vm.getCapability();*/
		                    VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();  
		                    System.out.println("---------------------------------------");
		                    System.out.println("Statistics of VM: " + vm.getName());
		                    System.out.println("Guest Host: " + vm.getConfig().guestFullName);
		                    System.out.println("Guest IP Address: " + vm.getSummary().guest.ipAddress);
		                    System.out.println("Available Memory: " + vm.getConfig().hardware.getMemoryMB());
		                    System.out.println("Private Memory: " + vm.getSummary().quickStats.getPrivateMemory());
		                    System.out.println("Max Memory Usage: " + vmri.maxMemoryUsage.toString());
		                    System.out.println("CPU Usage: " + vm.getSummary().quickStats.overallCpuUsage);
		                    System.out.println("Max CPU Usage: " + vmri.maxCpuUsage.toString());
		                    System.out.println("Uptime in seconds: " + vm.getSummary().quickStats.uptimeSeconds); 
		                    System.out.println("---------------------------------------");
                	}
                }
                System.out.println("---------Sleeping for 8 Minutes--------------");
                Thread.sleep(8 *60* 1000);//sleep for 8 minutes
                
            } catch (Exception e) 
            {
                System.out.println("Exception occured " + Arrays.toString(e.getStackTrace()));
            }

           
        }
    }
}