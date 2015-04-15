package controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class HostManager {
public ServiceInstance sInstance=null;

public static HashMap<HostSystem, List<VirtualMachine>> vhostVM=new HashMap<HostSystem, List<VirtualMachine>>();
	
HostManager(ServiceInstance sInstance) throws InvalidProperty, RuntimeFault, RemoteException{
		this.sInstance=sInstance;
		//hostManager();
	}
	public  HashMap<HostSystem, List<VirtualMachine>> hostManager() throws InvalidProperty, RuntimeFault, RemoteException{
		if (sInstance!=null) {
			Folder rootFolder = sInstance.getRootFolder();
			Folder hostFolder=sInstance.getRootFolder();
			ManagedEntity[] mesHost = new InventoryNavigator(hostFolder).searchManagedEntities("HostSystem");
			ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
			List<VirtualMachine> vms=new ArrayList<VirtualMachine>();			
			if (mes == null || mes.length == 0) {
				sInstance.getServerConnection().logout();
				return null;
			}
			
			com.vmware.vim25.mo.AlarmManager alaramManager=sInstance.getAlarmManager();
			util.AlaramUtil alarm=new util.AlaramUtil();
			//ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
			
			ArrayList<VirtualMachine>vmList=new ArrayList<VirtualMachine>();
			for(int i=0;i<mes.length;i++){
					//System.out.println(mes[i].getName());				
					VirtualMachine vm=(VirtualMachine)mes[i];
					vmList.add(vm);
					if(!vm.getConfig().template){
					alarm.createAlaram(vm, sInstance);
				}
			}
			
			for(int i=0;i<mesHost.length;i++){
				HostSystem host=(HostSystem) mesHost[i];
				VirtualMachine[] vm = (VirtualMachine[]) host.getVms();
				System.out.println(host.getName());
				for(int j=0;j<vm.length;j++){
					if(!vm[j].getConfig().template){
					vms.add(vm[j]);
					//System.out.println(vm[j].getName());
				}
				vhostVM.put(host,vms);
				}
			}
		}
		return vhostVM;
	}
}