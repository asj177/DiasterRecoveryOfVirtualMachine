package controller;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class SnapshotManager extends Thread {
	ServiceInstance sInstance, sAdminInstance = null;
	int duration;

	SnapshotManager(ServiceInstance sInstance, ServiceInstance sAdminInstance) 
	{
		this.sInstance = sInstance;
		this.sAdminInstance = sAdminInstance;
	}
	//new constructor
	
	
	public boolean checkCommand(String command, String ip) throws Exception 
	{
		String output = null;
		int count = 0;
		ProcessBuilder pb = new ProcessBuilder("ping", ip);
		Process process = pb.start();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		
		if(ip == null)
			System.out.println("Trying to Ping with IP as null");
		
		while ((output = stdInput.readLine()) != null) 
		{
			if (output.equals("Request timed out.")) 
			{
				if(count == 2)
					return false;
				count++;
			}
		}
		if ((output = stdError.readLine()) != null) 
		{
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("static-access")
	public void run() {
		while (true) 
		{
			try 
			{
				Folder rootFolder = sInstance.getRootFolder();
				ManagedEntity[] mes =new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
				ManagedEntity[] host = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
				String SnapshotManagername = "recent";
				String desc = "A description for sample SnapshotManager";
				
				for(int i=0;i<host.length;i++){
					
				for (int j = 0; j < mes.length; j++) 
				{
					VirtualMachine vm = (VirtualMachine) mes[j];
					HostSystem h=(HostSystem) host[i];
					if(!vm.getConfig().template){
					//Only Powered On VMs, IP address not null and OS status either Green or Gray
					if((h.getSummary().runtime.connectionState == h.getSummary().runtime.connectionState.connected)&& h.getSummary().runtime.powerState==h.getSummary().runtime.powerState.poweredOn ){
					if (vm.getSummary().runtime.powerState.toString().equals("poweredOn") && (vm.getGuest().getIpAddress() != null) && ((vm.getGuestHeartbeatStatus().toString().equals("green")) || (vm.getGuestHeartbeatStatus().toString().equals("gray"))))
					{
						//remove SnapshotManager
						Task removeTask = vm.removeAllSnapshots_Task();
						if (removeTask.waitForTask() == Task.SUCCESS)
							System.out.println("SnapshotManager removed successfully");//System.out.println("");
						else
							System.out.println(" No SnapshotManagers available for VM : "+ vm.getName());						
						//create new SnapshotManager
						Task createTask = vm.createSnapshot_Task(SnapshotManagername, desc,false, false);
						if (createTask.waitForTask()== Task.SUCCESS)
							System.out.println("SnapshotManager created for VM : "+ vm.getName());
						else
							System.out.println("SnapshotManager creation interrupted for VM :"+ vm.getName());
					}//if block
					}
				}
				}
				}//for loop
				//Host SnapshotManagers
				Folder rootAdminFolder = sAdminInstance.getRootFolder();
				//ManagedEntity[] mesHost = new InventoryNavigator(rootAdminFolder).searchManagedEntities("VirtualMachine");
				//Get number of Hosts
				ManagedEntity[] mesHost110 = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
				
				for (int j = 0; j < mesHost110.length; j++)
				{
					HostSystem hostMes=(HostSystem)mesHost110[j];
					ResourcePool res=(ResourcePool) new InventoryNavigator(rootAdminFolder).searchManagedEntity("ResourcePool","Team10_vHost");
							for(int k=0;k<res.getVMs().length;k++){
								System.out.println(res.getVMs()[k].getName().toString());
								if(res.getVMs()[k].getSummary().runtime.powerState.toString().equals("poweredOn")){
									Task task = res.getVMs()[k].removeAllSnapshots_Task();
									if (task.waitForTask() == Task.SUCCESS)
										;//System.out.println("");
									else
										System.out.println("No SnapshotManagers available for Host : "+ res.getVMs()[k].getName());
			//create new SnapshotManager
									task = res.getVMs()[k].createSnapshot_Task(SnapshotManagername, desc,false, false);
									if (task.waitForTask() == Task.SUCCESS)
										System.out.println("SnapshotManager created for Host : "+ res.getVMs()[k].getName());
									else
										System.out.println("SnapshotManager creation interrupted for Host : "+ res.getVMs()[k].getName());
								}
							}
						}
				
				System.out.println("Sleeping SnapshotManager Thread for " + "10 minutes");
				System.out.println();
				Thread.currentThread().sleep(1000*10*60);// sleep for 60 * X seconds
			//try
	}catch (Exception e) 
			{
				System.out.println("Exception while getting SnapshotManager: " + e.getMessage());
				System.out.println(e.getStackTrace().toString());
			}
			
		}
	}
}


