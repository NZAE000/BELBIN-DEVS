package controller;
import facade.modeling.FModel;
import facade.simulation.FCoupledSimulator;
import facade.simulation.FSimulator;
import facade.simulation.hooks.SimulatorHookListener;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import GenCol.entity;
import model.modeling.*;
import view.*;
import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.simView.*;

public class Controller implements ControllerInterface, SimulatorHookListener
{

    private FSimulator simulator;
    private ViewInterface view;
    private short modelType;
    protected ViewableDigraph instanceModel;
    static int sc;
   
    public static void main(String[] args)
    {

        new Controller();
        System.out.println("Welcome to the DEVS-Suite Simulation Environment!");
        System.out.println("To Begin, Select [Load Model...] From The [File] Menu");
    
    }
    
    public Controller() 
    {
        view = new View(this);
    }
    
    public void injectInputGesture(FModel model, String portName, entity input) 
    {
        model.injectInput(portName,input);
    }
    
    public void userGesture(String gesture, Object params) 
    {
        try
        {
            if (gesture.equals(SIM_RUN_GESTURE)){
                view.simlationControl(SIM_RUN_GESTURE);
            	simulator.run();
            	Stopwatch.start();
            }
            else if (gesture.equals(SIM_STEP_GESTURE)){
            	view.simlationControl(SIM_STEP_GESTURE);
                simulator.step();
            }
            else if (gesture.equals(SIM_STEPN_GESTURE)){
            	view.simlationControl(SIM_STEPN_GESTURE);
            	simulator.step(((Integer)params).intValue());
            }
            else if (gesture.equals(SIM_PAUSE_GESTURE)){
            	view.simlationControl(SIM_PAUSE_GESTURE);
            	simulator.requestPause();                
            }
            else if (gesture.equals(SIM_RESET_GESTURE))
            {
                view.simlationControl(SIM_RESET_GESTURE);
                simulator.reset();        
                tabbedPanel();
                view.loadSimulator(simulator);
                view.synchronizeView();
                Governor.reset();
                view.removeExternalWindows();
            }
            else if (gesture.equals(SIM_SET_RT_GESTURE))
                simulator.setRTMultiplier(((Double)params).doubleValue());
            else if (gesture.equals(SIM_SET_TV_GESTURE))
            	Governor.setTV(((Double)params).doubleValue());
            else if (gesture.equals(SAVE_TRACKING_LOG_GESTURE))
                writeString((String)params,view.getHTMLTrackingLog());
            else if (gesture.equals(SAVE_CONSOLE_LOG_GESTURE))
                writeString((String)params,view.getConsoleLog());
            else if (gesture.equals(LOAD_MODEL_GESTURE)){
            	tabbedPanel();
            	loadModel((String[]) params);
            }
            else if (gesture.equals(EXPORT_TO_CSV_GESTURE))
                writeString((String)params,view.getCSVExport());
            else if (gesture.equals(EXPORT_TO_ENCODED_CSV_GESTURE))
            {
                String[] data = view.getEncodedCSVExport();
                String[] paths = (String[]) params;
                writeString(paths[0],data[0]); 
                writeString(paths[1],data[1]);
            }
        }
        catch(Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }
    
    public void tabbedPanel(){
        View.tabbedPane.removeAll();
        View.tabbedPane.add(view.getConsole(), 0);
        View.tabbedPane.setTitleAt(0, "Console");
        view.clearConsole();
    }
    
    public void systemExitGesture() 
    {
        System.exit(0);
    }
    
    
    public void postComputeInputOutputHook() 
    {   
	    if(View.isTracking)
	    {
    		view.addTrackingColumn(simulator.getTimeOfNextEvent());
	    }
    	view.synchronizeView();
    }    
    
    public void simulatorStateChangeHook() 
    {
        view.synchronizeView();
    } 
   

    private void loadModel (String[] params)
    {
    	try
        {	
            Object instance;
          
		    try {
		    	view.clearConsole();
				URL urlList[] = {new File(System.getProperty("user.dir")).toURL()};
				
				
				ClassLoader loader = new URLClassLoader(urlList);
					
				   Class modelClass = loader.loadClass(params[0]+"."+params[1]);         
				
				  instance = modelClass.newInstance();
		    } catch (Exception en) {
		           en.printStackTrace();
		           return;
		    }
		    
		    if(instance instanceof ViewableAtomic){
		    	instanceModel = new ViewableDigraph("ViewableAtomic");		    	
		    	instanceModel.add((atomic)instance);
	            
		    	ViewableAtomic atomic = (ViewableAtomic)instance;
	            List names = atomic.getOutportNames();
	            for (int i = 0; i < names.size(); i++) {
	                String portName = (String)names.get(i);

	                instanceModel.addOutport(portName);
	                instanceModel.addCoupling(atomic, portName, instanceModel, portName);
	            }
	            
	            modelType = FModel.ATOMIC;
		    }
		    else if (instance instanceof ViewableDigraph){
		    	instanceModel = (ViewableDigraph)instance;
		    	modelType = FModel.COUPLED;
		    }
		   
		    simulator = new FCoupledSimulator(instanceModel, SimView.modelView, modelType);
		    simulator.setSimulatorHookListener(this);
		   	view.loadSimulator(simulator);		   
        }
        catch(Exception e)
        {
            System.err.println("An Error Occured While Loading Model: " + e);
            e.printStackTrace();
        }
    }
    
    private void writeString(String path, String stringToWrite)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            fw.write(stringToWrite);
            fw.close();
        }
        catch (Exception e)
        {
            System.err.println("An Error Occured While Writing: " + path);
            System.err.println(e);
        }
    }
}
