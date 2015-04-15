package util;

import java.rmi.RemoteException;

import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class AlaramUtil {
	private static final String  ALARM_NAME = "VmPowerStatus";
	private ServiceInstance sInstance=null;   //ServiceInstanceSingleton.getInstance().getServiceInstance();
	
	
	public void createAlaram(VirtualMachine vm,ServiceInstance sInstance) throws RuntimeFault, RemoteException{
		this.sInstance=sInstance;
		AlarmManager alaramManager=sInstance.getAlarmManager();
		Alarm[] alarms = alaramManager.getAlarm(vm);
		StateAlarmExpression expression=createStateAlarmExpression();
		Alarm virtualMachineAlarm=null;
		for(Alarm alarm:alarms){
			if(alarm.getAlarmInfo().getName().equals(ALARM_NAME) || alarm.getAlarmInfo().getName().equals(ALARM_NAME +"_"+vm.getName()) ){
				virtualMachineAlarm=alarm;
			}
			
		}
		
		if(virtualMachineAlarm!=null){
			System.out.println("Alarm already set for VM "+vm.getName());
			return;
		}
		
		
		AlarmSpec spec = new AlarmSpec();
		spec.setExpression(expression);
		spec.setName(ALARM_NAME+ "_"+vm.getName());
		spec.setDescription("Monitors the VM -State when the User powers off the VM ");
		spec.setEnabled(true);
		
		AlarmSetting as = new AlarmSetting();
		as.setReportingFrequency(0); //as often as possible
		as.setToleranceRange(0);
		
		spec.setSetting(as);
		
		alaramManager.createAlarm(vm, spec);
		System.out.println("Alarm created successfully");
		}
	
	
	
	public boolean checkAlaram(VirtualMachine vm, ServiceInstance sInstance) throws RuntimeFault, RemoteException{
		if(!vm.getConfig().template){
		AlarmState[]triggersAlarms=vm.getTriggeredAlarmState();
		Alarm[]alarms=sInstance.getAlarmManager().getAlarm(vm);
		Alarm valarm=null;
		
		for(Alarm alr:alarms){
			
			if(alr.getAlarmInfo().getName().equals(ALARM_NAME+"_"+vm.getName()) || alr.getAlarmInfo().getName().equals(ALARM_NAME) ){
				valarm=alr;
			}
		}
		
		if(valarm==null){
			System.out.println("Alarm not set for VM "+vm.getName());
			return false;
		}
		boolean flag=(triggersAlarms==null?false:true);
		if(flag==false){
			System.out.println("No Alarms Triggered ");
			return false;
			}else{
				for(AlarmState alarmState : triggersAlarms){
					if(alarmState.getAlarm().getVal().equals(valarm.getMOR().getVal()) && alarmState.overallStatus.name().equals("red")){
						return true;
					}
					
				}
			}
		}
		return false;
		
	}
	
	public boolean checkIfVmIsInTransition(VirtualMachine vm){
		if(!vm.getConfig().isTemplate()){
		AlarmState[]triggersAlarms=vm.getTriggeredAlarmState();
		if(triggersAlarms==null){
			return true;
		}
		}
		return false;
	}
	
	private StateAlarmExpression createStateAlarmExpression()
	  {
	    StateAlarmExpression expression = 
	      new StateAlarmExpression();
	    expression.setType("VirtualMachine");
	    expression.setStatePath("runtime.powerState");
	    expression.setOperator(StateAlarmOperator.isEqual);
	    expression.setRed("poweredOff");
	    return expression;
	  }

}

