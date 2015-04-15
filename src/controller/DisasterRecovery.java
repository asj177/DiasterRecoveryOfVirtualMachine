package controller;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import view.PerformanceStats;

import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.HostSslThumbprintInfo;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

import connectors.Connectors;

	@SuppressWarnings("unused")
	public class DisasterRecovery {		
		public static ServiceInstance sInstance;
		public static ServiceInstance sInstance2;
		public DisasterRecovery() throws MalformedURLException, RemoteException{
			Connectors vmInfo=new Connectors();
			sInstance=vmInfo.getConn();
			sInstance2=vmInfo.getAdminConn();
		}
		
	    public static void main(String[] args) throws Exception {
	           DisasterRecovery m=new DisasterRecovery();
	           HostManager vhostVM =new HostManager(sInstance);
	           HashMap<HostSystem, List<VirtualMachine>> vhostVMs=new HashMap<HostSystem, List<VirtualMachine>>();
	           vhostVMs=vhostVM.hostManager();
	           SnapshotManager snap=new SnapshotManager(sInstance, sInstance2);
	           snap.start();
               HeartBeat heartBeat=new HeartBeat(vhostVMs, sInstance);
	           heartBeat.start();
	           PerformanceStats stats =new PerformanceStats(sInstance);
	           stats.start();
		}

}
