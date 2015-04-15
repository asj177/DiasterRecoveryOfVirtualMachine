package controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import util.AlaramUtil;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.MigrationFault;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.Timedout;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import connectors.Connectors;


public class RecoveryManager {

	public ServiceInstance sInstance;
	public HostSystem vHost;
	//HashMap<HostSystem, List<VirtualMachine>> vhostVM=null;
	
	@SuppressWarnings("static-access")
	public boolean recoverVMs(ServiceInstance sInstance,HostSystem host, VirtualMachine vm) throws Exception{
		Connectors adminConn=new Connectors();
		ServiceInstance sAdminInstance=adminConn.getAdminConn();
		Folder rootAdminFolder = sAdminInstance.getRootFolder();
		System.out.println("Hostname: "+ host.getName().toString()+ "Vm"+":"+vm.getName().toString());
		//Recover VMs on the same host:
		String hostip=host.getName().toString();
		Folder rootFolder = sInstance.getRootFolder();
		HostSystem hostCheck=(HostSystem)new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostip);
		if(hostCheck!=null && vm!=null){
			System.out.println("Hostname: "+ host.getName().toString()+ "Vm"+":"+vm.getName().toString());
			if(hostCheck.getSummary().runtime.powerState == hostCheck.getSummary().runtime.powerState.poweredOn){
				Task task = vm.revertToCurrentSnapshot_Task(null);
				if (task.waitForTask()==Task.SUCCESS) {
					System.out.println("Congratulations....VM has been recovered..");
				}
				System.out.println("Powering On VM on the same host");
				Task taskVm = vm.powerOnVM_Task(null);
				if (taskVm.waitForTask()==Task.SUCCESS) {
					System.out.println("Congratulations....VM has been Successfully Powered On..");
				}
				return true;

			}else if(hostCheck.getSummary().runtime.powerState==hostCheck.getSummary().runtime.powerState.poweredOff ||
					hostCheck.getSummary().runtime.connectionState == hostCheck.getSummary().runtime.connectionState.disconnected  ){
				//Folder rootAdminFolder1 = sAdminInstance.getRootFolder();
				ManagedEntity currentH =null;
//				
//				System.out.println(hostCheck.getName());
				ManagedEntity[] mesHost110 = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
				for (int j = 0; j < mesHost110.length; j++)
				{	HostSystem hostMes=(HostSystem)mesHost110[j];
					ResourcePool res=(ResourcePool) new InventoryNavigator(rootAdminFolder).searchManagedEntity("ResourcePool","Team10_vHost");
						for(int k=0;k<res.getVMs().length;k++){
							System.out.println(res.getVMs()[k].getName().toString());
							if(res.getVMs()[k].getName().toString().contains("130.223")){
								currentH = res.getVMs()[k];
							}
						}
				}
				VirtualMachine currentHost=(VirtualMachine)currentH;
				//VirtualMachine currentHost=(VirtualMachine) currentH;
				Task task = currentHost.powerOnVM_Task(null);
				if(Task.SUCCESS==task.waitForTask()){
					System.out.println("OOOOOO  Started");
				}else{
					System.out.println("MayDay MayDay!!!! Houston, we gotta problem");
				}
				System.out.println("Trying to reconnect....");
			//	vCenterConfig vCenter=new vCenterConfig();
				System.out.println("Trying to reconnect vHost...");
				Thread.sleep(1000*60*2);
				for(int a=0;a<3;a++){
					System.out.println("Attempt no -"+a);
					Task reconnectTask = hostCheck.reconnectHost_Task(null);
					while (reconnectTask.getTaskInfo().state == reconnectTask.getTaskInfo().state.running) {
						System.out.print(".");
					}
					if(Task.SUCCESS==reconnectTask.waitForTask()){
						System.out.println("OOOOOO  Started");
					}else{
						System.out.println("MayDay MayDay!!!! Houston, we gotta problem");
					}
					HostSystem destHost=null;
					//System.out.println(currentHost.getName().toString());
					if(currentHost.getName().toString().contains("T10-vHost03_130.223")){
						Folder rootdestFolder=sInstance.getRootFolder();
						ManagedEntity dest=new InventoryNavigator(rootdestFolder).searchManagedEntity("HostSystem","130.65.132.222");
						destHost=(HostSystem)dest;
					}else{
						Folder rootdestFolder=sInstance.getRootFolder();
						ManagedEntity dest=new InventoryNavigator(rootdestFolder).searchManagedEntity("HostSystem","130.65.132.223");
						destHost=(HostSystem)dest;
					}
					if(migrateVM(destHost.getName().toString(),sInstance,vm.getName().toString())){
						Task migrate=vm.revertToCurrentSnapshot_Task(null);
						if(Task.SUCCESS==migrate.waitForTask()){
							System.out.println("/*****  After migration VM Started******/");
						}else{
							System.out.println("MayDay MayDay!!!! Houston, we gotta problem");
						}
						Task powerUp=vm.powerOnVM_Task(null);
						if(Task.SUCCESS==powerUp.waitForTask()){
							System.out.println("/***** Powered up after migration *****/");
						}else{
							System.out.println("MayDay MayDay!!!! Houston, powering up gotta problem");
						}
						AlaramUtil alarm=new AlaramUtil();
						alarm.createAlaram(vm,sInstance);
					}
					return true;
				}
				return false;
			}else{
				
				int j=0;
				ManagedEntity[] vhosts=new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
				//HostSystem[] vhosts=(HostSystem[])new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
				List<HostSystem> vHosts =new ArrayList<HostSystem>() ;
				for(int i=0;i<vhosts.length;i++){
					vHosts.add((HostSystem)vhosts[j]);
					j++;
				}
				if (vHosts.size() != 1) {
					for (HostSystem vHost : vHosts) {
						if (vHost.getSummary().runtime.powerState == vHost
								.getSummary().runtime.powerState.poweredOn) {							
							if(migrateVM(vHost.getName().toString(),sInstance,vm.getName().toString())){
								AlaramUtil alarm=new AlaramUtil();
								alarm.createAlaram(vm,sInstance);
								return true;
							}
						}				
					}				
				} else{
					VirtualMachine currentHost = (VirtualMachine) new InventoryNavigator(rootAdminFolder).searchManagedEntity("HostName",hostCheck.getName().toString());
					System.out.println("**********************************************************");
					System.out.println("Recovering Current Host from failure");
					System.out.println("Host is being recovered");
					//VirtualMachine currentHost = (VirtualMachine) new InventoryNavigator(rootAdminFolder).searchManagedEntity("HostName",host.getName().toString());
					Task taskHost1 = currentHost.revertToCurrentSnapshot_Task(null);
					if (taskHost1.getTaskInfo().getState().success == TaskInfoState.success) {
						System.out.println("recovering Host at admin vCenter..");
					}else{
						System.out.println("recovering Host at admin vCenter..");
					}
					return true;
				}
				}
			}
		
		return false;
	}
		
	/**
	 * Migrate VM from one source to another 
	 * @param newhost
	 * @param sInstance
	 * @param vmName
	 * @return
	 * @throws VmConfigFault
	 * @throws Timedout
	 * @throws FileFault
	 * @throws InvalidState
	 * @throws InsufficientResourcesFault
	 * @throws MigrationFault
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	
		@SuppressWarnings("deprecation")
	public boolean migrateVM(String newhost, ServiceInstance sInstance, String vmName) throws Exception{
		String vmname = vmName;
		String newHostName = newhost;
		Folder rootFolder = sInstance.getRootFolder();
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
													rootFolder).searchManagedEntity(
																"VirtualMachine", vmname);
		
		HostSystem newHost = (HostSystem) new InventoryNavigator(
											rootFolder).searchManagedEntity(
														"HostSystem", newHostName);
		
		ComputeResource cr = (ComputeResource) newHost.getParent();
		String[] checks = new String[] {"cpu", "software"};
		HostVMotionCompatibility[] vmcs =
											sInstance.queryVMotionCompatibility(vm, new HostSystem[] 
															{newHost},checks );

		String[] comps = vmcs[0].getCompatibility();
		if(checks.length != comps.length)
		{
			System.out.println("CPU/software NOT compatible. Exit.");
			sInstance.getServerConnection().logout();
			return false;
		}
		
		if(vm.getSummary().runtime.powerState==vm.getSummary().runtime.powerState.poweredOn){
			Task task =vm.powerOffVM_Task();
				if(task.waitForTask()==Task.SUCCESS){
					System.out.println("PoweredOff");
				}else{
					System.out.println("Error while powering off the vm.");
				}
		}
		
		Task task1 = vm.migrateVM_Task(cr.getResourcePool(), newHost,
												VirtualMachineMovePriority.highPriority, 
														VirtualMachinePowerState.poweredOff);

		if(task1.waitForMe()==Task.SUCCESS)
		{
			System.out.println("  Migrated");
			return true;
		}
		else
		{
			System.out.println("Migrated failed!");
			TaskInfo info = task1.getTaskInfo();
			System.out.println(info.getError().getFault());
			return false;
		}
	}
		
		public boolean reconnectHost(HostSystem host, ServiceInstance sAdminInstance) throws RuntimeFault, RemoteException, InterruptedException{
			if(host.getSummary().runtime.powerState==host.getSummary().runtime.powerState.poweredOff){
//				ManagedEntity hostPower=new InventoryNavigator(rootAdmin).searchManagedEntity("HostSystem", host.getName().toString());
//				Task power = host.reconnectHost_Task(null);
				Folder rootAdminFolder=sAdminInstance.getRootFolder();
				//HostSystem hostMes=(HostSystem)mesHost110[j];
				ResourcePool res=(ResourcePool) new InventoryNavigator(rootAdminFolder).searchManagedEntity("ResourcePool","Team10_vHost");
				for(int k=0;k<res.getVMs().length;k++){
					System.out.println(res.getVMs()[k].getName().toString());
					if(res.getVMs()[k].getSummary().runtime.powerState.toString().equals("poweredoff")){
						Task task = res.getVMs()[k].powerOnVM_Task(null);
						if(Task.SUCCESS==task.waitForTask()){
							System.out.println("OOOOOO  Started");
						}else{
							System.out.println("MayDay MayDay!!!! Houston, we gotta problem");
						}
			}
						Task reconnectTask = host.reconnectHost_Task(null);
						if(Task.SUCCESS==reconnectTask.waitForTask()){
							System.out.println("OOOOOO  Started");
						}else{
							System.out.println("MayDay MayDay!!!! Houston, we gotta problem");
						}
						Thread.sleep(1000*60*5);
						return true;
				}
			}
			return false;
		}
	/**
	 * Powering On VM
	 * @param vm
	 * @throws Exception
	 */
	
	public void powerOn(VirtualMachine vm) throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println(vm.getName() + " is powering on...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is running now.");
	}
	
	/**
	 * Poweroff VM
	 * @param vm
	 * @throws Exception
	 */

	public void powerOff(VirtualMachine vm) throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println(vm.getName() + " is powering off...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is shut down.");
	}
	
	/**
	 * Recover VM from Crash
	 * @param sInstance
	 * @param host
	 * @throws Exception
	 */
	
	
}


