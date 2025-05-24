package MyPackage;
import view.modeling.ViewableDigraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    private List<Person> group               			 = new ArrayList<>();
    private List<String> groupPersonalities  			 = new ArrayList<>();
    private Map<String, Double> personalitySpeakingTimes = new HashMap<>();
    private List<List<Object>> personalData  			 = null;
 
    // CONSTRUCTOR #######################################################################
    public GroupCoupled() {
        super("GroupCoupled");
        initializeModel();
    }

    private void initializeModel() 
    {
    	readPersonalitySpeakingTime("input_data/speakingtime/test1.txt");
    	readGroup("input_data/group/test1.json");
    	initGroup();				// Initialize personality combinations and time speak.
    	setPorts(); 				// Set in/out ports for all person atomic models.
    	setCoupling(); 				// Establish coupling.
        initInteractionHz(group); 	// Initialize matrix frecuency.
        showInteractionHz();		// Show matrix frecuency.
        
        // Show all group personality and persons.
        //for (int i = 0; i < numPerson; i++) {
        	//System.out.println("Personalidades persona " + group.get(i).getName() + ": " + group.get(i).getgroupPersonalities().toString());
        	//System.out.println("group " + group.get(i).getName() + ": " + group.get(i).getPersons().toString());
        //}
        //initialize();
    }
    
// Helper methods ################################################################
    
    private void readPersonalitySpeakingTime(String path)
    {
    	// Read personality combinations.
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length != 2) {
                    System.out.println("Malformed line: " + line);
                    continue;
                }
                personalitySpeakingTimes.put(parts[0], Double.parseDouble(parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void readGroup(String path) 
    {
		// Read json.
		JSONReader jsonreader = new JSONReader();
		personalData = jsonreader.read(
			path,
		    Arrays.asList("nombre", "personalidad1", "personalidad2", "porcentaje1", "porcentaje2"),
		    Arrays.asList(String.class, String.class, String.class, Double.class, Double.class)
		);
    }
    
    private void initGroup()
    {
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
        
        // Set speaking time for all persons.
        for (Person person : group) this.setSpeakingTimeOf(person);
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

	        if (personalitySpeakingTimes.containsKey(combination1)) {
	            speakingTime += personalitySpeakingTimes.get(combination1);
	        }
	        if (personalitySpeakingTimes.containsKey(combination2)) {
	            speakingTime += personalitySpeakingTimes.get(combination2);
	        }
	    }
	    person.setTimeSpeak(speakingTime*avgPercentages);
	    System.out.println("Speaking time of " + person.getName() + ": " + person.getTimeSpeak()); // Speaking time of Charlot: 36.300000000000004
	}
    
    private void setPorts() 
    {
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
    }
    
    private void setCoupling() 
    {
    	// Establish coupling.
        for (int i = 0; i < GroupSize; i++) {
            for (int k = 0; k < GroupSize; k++) {
                if (i != k) {
                    addCoupling(group.get(i), "Outport " + group.get(k).getName(), group.get(k), "Inport " + group.get(k).getName());
                }
            }
        }
    }

    
 // Metric methods ################################################################
    
    public static void initInteractionHz(List<Person> group)
    {
    	Frecuencies.clear();
    	int numPerson = GroupSize;
    	for (int i=0; i < numPerson; i++) {
        	String from = group.get(i).getName();
			for (int j=0; j < numPerson; j++) {
				String to = group.get(j).getName();
				addInteractionHz(from, to, 0);
			}
        }
    }
    
    public static void addInteractionHz(String from, String to, int hz)
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
     
     public static void showInteractionHz()
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
    	 initInteractionHz(group);
     }
}
