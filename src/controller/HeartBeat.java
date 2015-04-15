package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.AlaramUtil;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

@SuppressWarnings("unused")
public class HeartBeat extends Thread implements Runnable{
	HashMap<HostSystem, List<VirtualMachine>> vhostVM=null;
	//ArrayList<VirtualMachine>vmList;
	ServiceInstance sInstance=null;
	AlaramUtil checkAlaram=new AlaramUtil();
	public HeartBeat(HashMap<HostSystem, List<VirtualMachine>> vhostVM, ServiceInstance sInstance){
		this.vhostVM=vhostVM;
		this.sInstance=sInstance;
	}
	@SuppressWarnings("deprecation")
	public void run() {
		// TODO Auto-generated method stub
		
		//HostSystem hosts=(HostSystem) vhostVM.entrySet();
		try{																																																																					
			while(true){
				
					Folder rootFolder = sInstance.getRootFolder();
					ManagedEntity[] hosts=new  InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
					ManagedEntity[] vms1=new  InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
					
					for(int i=0;i<hosts.length;i++){
						HostSystem host=(HostSystem) hosts[i];
					if(!pingHost(host.getName().toString())){
						VirtualMachine[]vms=host.getVms();
				     	for(int j=0;j<vms.length;j++){
				     		VirtualMachine vm=(VirtualMachine) vms[j];
				     	
						if(!vm.getConfig().template){
							Date todaysDate=new Date();
							//String ip= ((vm.getGuest().getIpAddress().toString())!=null)?(vm.getGuest().getIpAddress().toString()):null;
							//vm.getParent();
							if(pingVM(host,vm, sInstance)){
								int count=0;
								boolean intransition=false;
								while(checkIfVMIsInTransition(vm)){
									intransition=true;
									System.out.print("VM Transition in Progress..Please Wait"+" ");
									System.out.println(vm.getName().toString());
									count++;
									Thread.sleep(1*60*1000);
									if(!pingVM(host,vm, sInstance)){
										break;
									}
									if(count==3){
										System.out.println("Transition Time exceeded");
										intransition=false;
										break;
									}
								}
								boolean flag=checkAlaram.checkAlaram(vm, sInstance);
								if( !intransition && !(flag)){
									System.out.println(vm.getName().toString());
									System.out.println("Run recovery");
									RecoveryManager rManager=new RecoveryManager();
									
									rManager.recoverVMs(sInstance, host, vm);
									//recoverVMs(ServiceInstance sInstance,HostSystem host, VirtualMachine vm)
								}else{
									System.out.println(todaysDate.toGMTString() +" : Ping Failed as VM"+" "+vm.getName()+" "+"shut down by user hence no corrective action executed");
								}
							}else{
								System.out.println(todaysDate.toGMTString() +" : Ping to VM "+vm.getName()+" "+vm.getGuest().getIpAddress().toString() + " Successful ");
						}
						Thread.sleep(1000*60*5);
					}
				}}
			}
					}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private boolean pingHost(String ip){
		boolean fail=false;
		try {
			Process p=Runtime.getRuntime().exec("ping -c 1"+ip);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        if(ip==null){
	        	return true;
	        }
	        
	        String output="";
	        int wait=0;
	        while((output=in.readLine())!=null){
	         	
	         	if(output.equalsIgnoreCase("Request timed out")){
	         		wait++;
	         		if(wait==3){
	         			fail=true;
	         			break;
	         		}
	         	}
	         }
	         in.close();
	         er.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fail;
	}
	private  boolean pingVM(HostSystem host, VirtualMachine vm, ServiceInstance sInstance) {
		boolean fail=false;
		try{
			String hostip=host.getName().toString();
			//String ipStr=vm.getGuest().getIpAddress().toString();
			Folder rootFolder = sInstance.getRootFolder();
			HostSystem hostCheck=(HostSystem)new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostip);
			//ManagedEntity vmCheck=new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", ip);
			//VirtualMachine v=(VirtualMachine) vmCheck;
			System.out.println(hostCheck.getName().toString());
			if(hostCheck.getSummary().runtime.powerState == hostCheck.getSummary().runtime.powerState.poweredOn){
				Process p=Runtime.getRuntime().exec("ping -c 1"+vm.getGuest().getIpAddress());  //v.getGuest().getIpAddress().toString());
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        System.out.println(er);
	       // System.out.println(er.readLine());
	       // System.out.println("error read line is "+er.readLine());
	        if(vm.getGuest().getIpAddress()==null){
	        	return true;
	        }
	        
	        boolean flag= (er.readLine()!=null? true:false);
	        if(flag){
	        	return true;
	        }
//	       if(er.readLine()!=null){
//	        	in.close();
//	        	er.close();
//	        	return true;
//	        }
	        
			
			
        String output="";
        int wait=0;
         while((output=in.readLine())!=null){
         	
         	if(output.equalsIgnoreCase("Request timed out")){
         		wait++;
         		if(wait==3){
         			fail=true;
         			break;
         		}
         	}
         }
         in.close();
         er.close();
	}else{
		return true;
	}
		}catch(Exception e){
			e.printStackTrace();
		}
         return false;
		
		
	}
	
	public boolean checkIfVMIsInTransition(VirtualMachine vm)
	{
		AlaramUtil alarm=new AlaramUtil();
		boolean transitionState=alarm.checkIfVmIsInTransition(vm);
		return transitionState;
	}
	
}
