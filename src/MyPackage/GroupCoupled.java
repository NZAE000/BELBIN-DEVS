package MyPackage;
import view.modeling.ViewableDigraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupCoupled extends ViewableDigraph {
	
	public static class SpeakingTime {
		public SpeakingTime(String nme, Double tElapse) {
			this.name       = nme;
			this.timeElapse = tElapse;
		}
		public String name       = null;
		public Double timeElapse = 0.0;
	}
	
	// STATIC MEMBERS ####################################################################
	public static final double TotalTimeSim = 500;
	public static double ElapsedTime        = 0.0;
	public static int GroupSize 		    = 0;
	// Metrics
	private static HashMap<String, Map<String, Integer>> Frecuencies = new HashMap<>();
	private static HashMap<String, Double> SpeakingTimeAccumulator   = new HashMap<>();
	private static ArrayList<SpeakingTime> SpeakingTimes 			 = new ArrayList<>();
	
	// ATRIBUTES #########################################################################
    private List<Person> group               = new ArrayList<>();
    private List<String> groupPersonalities  = new ArrayList<>();
    private List<List<Object>> personalData  = null;
    private Map<String, Double> additionalSpeakingTimes = new HashMap<>();
 
    // CONSTRUCTOR #######################################################################
    public GroupCoupled() {
        super("GroupCoupled");
        initializeModel();
    }

    private void initializeModel() 
    {	
    	// Initialize personality combinations and time speak.
    	initPersonalityTimeSpeak();
    	
    	// Read json.
    	JSONReader jsonreader = new JSONReader();
    	personalData = jsonreader.read(
		    "Personas.json",
		    Arrays.asList("nombre", "personalidad1", "personalidad2", "porcentaje1", "porcentaje2"),
		    Arrays.asList(String.class, String.class, String.class, Double.class, Double.class)
		);
    	
        int numPerson = GroupSize = personalData.size();
        
        // Init all person atomic models.
        for (int i = 0; i < numPerson; i++) {
        	List<Object> data   = personalData.get(i);
        	String name         = data.get(0).toString();
        	String personality1 = data.get(1).toString();
        	String personality2 = data.get(2).toString();
        	double percentaje1  = Double.parseDouble(data.get(3).toString());
        	double percentaje2  = Double.parseDouble(data.get(4).toString());
            group.add(new Person(
            	name,
            	personality1,
            	personality2,
            	percentaje1,
            	percentaje2,
                group
            ));
            add(group.get(i));
            
            // Add the personalities if are not.
            //if (!groupPersonalities.contains(personality1))
            	 groupPersonalities.add(personality1);
            //if (!groupPersonalities.contains(personality2))
            	groupPersonalities.add(personality2);
        }
        
        // Set speak time for all persons
        for (Person person : group) this.setSpeakingTimeOf(person);
        
        // Set in/out ports for all person atomic models.
        for (Person person1 : group) {
        	for (Person person2 : group) {
                String personName = person2.getName();
                if (!personName.equals(person1.getName())) {
                	person1.addInport("Inport " + personName);
                	person1.addOutport("Outport " + personName);
                }
            }
        }
        
        // Show all group personality and persons.
        //for (int i = 0; i < numPerson; i++) {
        	//System.out.println("Personalidades persona " + group.get(i).getName() + ": " + group.get(i).getgroupPersonalities().toString());
        	//System.out.println("group " + group.get(i).getName() + ": " + group.get(i).getPersons().toString());
        //}
        
        // Initialize matrix frecuency.
        initComunicationHz(group);
        
        // Show matrix frecuency.
        showInteractionFrecuency();
        
        // Establish coupling.
        for (int i = 0; i < numPerson; i++) {
            for (int k = 0; k < numPerson; k++) {
                if (i != k) {
                    addCoupling(group.get(i), "Outport " + group.get(k).getName(), group.get(k), "Inport " + group.get(k).getName());
                }
            }
        }
        //initialize();
    }
    
    private void initPersonalityTimeSpeak()
    {
    	// Initialize personality combinations.
	    
	    // 1
	    /*
	    additionalSpeakingTimes.put("Investigador de Recursos-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Especialista", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Implementador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Finalizador ", 3.0);
    	additionalSpeakingTimes.put("Cohesionador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cohesionador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Cohesionador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Coordinador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Coordinador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Cohesionador", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Coordinador", 4.0);
	    additionalSpeakingTimes.put("Cerebro-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Finalizador ", 3.0);
	   	additionalSpeakingTimes.put("Especialista-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Especialista-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Especialista-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Especialista-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Especialista", 3.0);
	    additionalSpeakingTimes.put("Especialista-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Especialista-Implementador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Impulsor-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Impulsor-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Especialista", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Implementador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Implementador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Implementador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Implementador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Implementador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Implementador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Implementador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Finalizador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Finalizador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Finalizador ", 3.0);
	    //*/
	    
	    // 2
	    /*
	    additionalSpeakingTimes.put("Investigador de Recursos-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Especialista", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Implementador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Finalizador ", 3.0);
    	additionalSpeakingTimes.put("Cohesionador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cohesionador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Cohesionador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Coordinador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Coordinador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Cohesionador", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Coordinador", 4.0);
	    additionalSpeakingTimes.put("Cerebro-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Finalizador ", 3.0);
	   	additionalSpeakingTimes.put("Especialista-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Especialista-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Especialista-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Especialista-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Especialista", 3.0);
	    additionalSpeakingTimes.put("Especialista-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Especialista-Implementador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Impulsor-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Impulsor-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Especialista", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Implementador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Implementador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Implementador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Implementador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Implementador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Implementador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Implementador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Finalizador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Finalizador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Finalizador ", 3.0);
	    //*/
	    
	    // 3
	    /*
	 	additionalSpeakingTimes.put("Investigador de Recursos-Investigador de Recursos", 1.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cohesionador", 2.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Especialista", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Implementador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Finalizador ", 3.0);
    	additionalSpeakingTimes.put("Cohesionador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cohesionador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Cohesionador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Coordinador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Coordinador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Cohesionador", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Coordinador", 4.0);
	    additionalSpeakingTimes.put("Cerebro-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Finalizador ", 3.0);
	   	additionalSpeakingTimes.put("Especialista-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Especialista-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Especialista-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Especialista-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Especialista", 3.0);
	    additionalSpeakingTimes.put("Especialista-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Especialista-Implementador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Impulsor-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Impulsor-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Especialista", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Implementador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Implementador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Implementador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Implementador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Implementador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Implementador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Implementador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Finalizador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Finalizador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Impulsor", 2.0);
	    additionalSpeakingTimes.put("Finalizador-Implementador", 1.0);
	    additionalSpeakingTimes.put("Finalizador-Finalizador ", 4.0); 
	    //*/
	    
	    // 4
	    ///*
	    additionalSpeakingTimes.put("Investigador de Recursos-Investigador de Recursos", 1.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cohesionador", 2.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Especialista", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Implementador", 3.0);
	    additionalSpeakingTimes.put("Investigador de Recursos-Finalizador ", 3.0);
    	additionalSpeakingTimes.put("Cohesionador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cohesionador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Cohesionador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cohesionador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Coordinador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Coordinador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Coordinador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Cohesionador", 2.0);
	    additionalSpeakingTimes.put("Cerebro-Coordinador", 4.0);
	    additionalSpeakingTimes.put("Cerebro-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Especialista", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Implementador", 3.0);
	    additionalSpeakingTimes.put("Cerebro-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Monitor Evaluador-Finalizador ", 3.0);
	   	additionalSpeakingTimes.put("Especialista-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Especialista-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Especialista-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Especialista-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Especialista", 3.0);
	    additionalSpeakingTimes.put("Especialista-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Especialista-Implementador", 3.0);
	    additionalSpeakingTimes.put("Especialista-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Impulsor-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Impulsor-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Especialista", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Implementador", 3.0);
	    additionalSpeakingTimes.put("Impulsor-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Implementador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Implementador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Implementador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Implementador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Implementador-Impulsor", 3.0);
	    additionalSpeakingTimes.put("Implementador-Implementador", 3.0);
	    additionalSpeakingTimes.put("Implementador-Finalizador ", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Investigador de Recursos", 2.0);
	    additionalSpeakingTimes.put("Finalizador-Cohesionador", 1.0);
	    additionalSpeakingTimes.put("Finalizador-Coordinador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Cerebro", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Monitor Evaluador", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Especialista", 3.0);
	    additionalSpeakingTimes.put("Finalizador-Impulsor", 2.0);
	    additionalSpeakingTimes.put("Finalizador-Implementador", 1.0);
	    additionalSpeakingTimes.put("Finalizador-Finalizador ", 4.0);
	    //*/
    }
    
    private void setSpeakingTimeOf(Person person) 
    {
	    double speakingTime   = 0.0;
	    double avgPercentages = (person.getPercentage1() + person.getPercentage2()) / 2.0;
	    String personality1   = person.getPersonality1();
	    String personality2   = person.getPersonality2();
	    
	    for (String personality : this.groupPersonalities) {
	        String combination1 = personality1 + "-" + personality;
	        String combination2 = personality2 + "-" + personality;

	        if (additionalSpeakingTimes.containsKey(combination1)) {
	            speakingTime += additionalSpeakingTimes.get(combination1);
	        }
	        if (additionalSpeakingTimes.containsKey(combination2)) {
	            speakingTime += additionalSpeakingTimes.get(combination2);
	        }
	    }
	    person.setTimeSpeak(speakingTime*avgPercentages);
	    System.out.println("Speaking time of " + person.getName() + ": " + person.getTimeSpeak()); // Speaking time of Charlot: 36.300000000000004
	}
    
 // Metric methods ################################################################
    
    public static void initComunicationHz(List<Person> group)
    {
    	Frecuencies.clear();
    	int numPerson = GroupSize;
    	for (int i=0; i < numPerson; i++) {
        	String from = group.get(i).getName();
			for (int j=0; j < numPerson; j++) {
				String to = group.get(j).getName();
				addComunicationHz(from, to, 0);
			}
        }
    }
    
    public static void addComunicationHz(String from, String to, int hz)
    {
    	if (!Frecuencies.containsKey(from))
    		Frecuencies.put(from, new HashMap<>());
        
        Map<String, Integer> frecuency = Frecuencies.get(from); // Get frecuency map 'from'.
        frecuency.put(to, frecuency.getOrDefault(to, 0) + hz);	// There is no key 'to' in the map, initialize to 0.
    }
	
 	 public static void addSpeakingTime(String name, Double time) {
     	SpeakingTimes.add(new SpeakingTime(name, time));
     }
     
     public static void accumulateSpeakingTimeHz(String from, Double speak_time_lapse) {
     	SpeakingTimeAccumulator.put(from, SpeakingTimeAccumulator.getOrDefault(from, 0.0) + speak_time_lapse);
     }
     
     public static void showInteractionFrecuency()
     {
     	for (Map.Entry<String, Map<String, Integer>> entry : Frecuencies.entrySet()) {
             String name = entry.getKey();
             System.out.print("\t" + name);
         }
         System.out.println();
         for (Map.Entry<String, Map<String, Integer>> entry : Frecuencies.entrySet()) {
             String from = entry.getKey();
             System.out.print(from);
             Map<String, Integer> to = entry.getValue();

             for (Map.Entry<String, Integer> entryInterno : to.entrySet()) {
                 Integer hz = entryInterno.getValue();
                 System.out.print("\t" + hz);
             }
             System.out.println();
         }
     }
     
     public static void showSpeakTime() 
     {
     	 if(ElapsedTime > TotalTimeSim) {
     		 System.out.println("\nName\tSpeak time");
     		 //for (Map.Entry<String, Double> entry : SpeakingTimeAccumulator.entrySet()) 
     		 //{
     		//	 String speaker_name = entry.getKey();
     		//	 Double total_time_speaking = entry.getValue();
     		//	 Double percentage = total_time_speaking / GroupCoupled.ElapsedTime;
     		//	 System.out.println(speaker_name + "\t" + percentage + "\n");
     		 //}
     		 Double total_time = 0.0;
     		 for (int i = 0; i < SpeakingTimes.size(); i++) {
     			 System.out.println(SpeakingTimes.get(i).name + "\t" + SpeakingTimes.get(i).timeElapse + "\n");
     			 total_time += SpeakingTimes.get(i).timeElapse;
 			 }
     		 System.out.println("Total\t" + total_time + "\n");
     		 //SpeakingTimes.forEach( (speaktime) -> { System.out.println(speaktime.name + "\t" + speaktime.timeElapse + "\n"); } );
     	 }
     }
     
     public static void showSpeakTimeRate() 
     {
     	 if(ElapsedTime > TotalTimeSim) {
     		 System.out.println("Name\tSpeak rate");
     		 double totalRate = 0.0, total_time_speaking = 0.0, percentage = 0.0;
     		 for (Map.Entry<String, Double> entry : SpeakingTimeAccumulator.entrySet()) 
     		 {
     			 String speaker_name = entry.getKey();
     			 total_time_speaking = entry.getValue();
     			 percentage = total_time_speaking / ElapsedTime;
     			 totalRate += percentage;
     			 System.out.println(speaker_name + "\t" + percentage + "\n");
     		 }
     		 System.out.println("Total:" + "\t" + totalRate + "\n");
     	 }
     }
     
     public static void restoreMetrics(List<Person> group)
     {
    	 ElapsedTime = 0.0;
    	 SpeakingTimes.clear();
    	 SpeakingTimeAccumulator.clear();
    	 initComunicationHz(group);
     }
}
