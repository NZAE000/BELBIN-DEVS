package MyPackage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.modeling.message;
import view.modeling.ViewableAtomic;
import GenCol.*;


public class Person extends ViewableAtomic {
	
// ATRIBUTES #########################################################################
    private String name 		   = null;
    private String personality1    = null;
    private String personality2    = null;
    private double percentage1     = 0.0;
    private double percentage2     = 0.0;
    private List<Person> group     = null;
	private boolean listening 	   = false;
    private boolean talking 	   = false;
    boolean generatedMessage       = false;
    private double speakingTime    = 0.0;
    private double accumSpokenTime = 0.0;
    //CSVWriter csvWriter            = new CSVWriter("resultados.csv");
    
 // CONSTRUCTOR #########################################################################
    public Person(String name, String personality1, String personality2, double percentage1, double percentage2, List<Person> group) {
        super(name);
        this.name         = name;
        this.personality1 = personality1;
        this.personality2 = personality2;
        this.percentage1  = percentage1;
        this.percentage2  = percentage2;
        this.group        = group;
    }
    
// METHODS #########################################################################    
    public String getName() 		  		{ return this.name;           }
    public String getPersonality1()   		{ return this.personality1;   }
    public String getPersonality2()   	    { return this.personality2;   }
    public double getPercentage1()    	    { return this.percentage1;    }
    public double getPercentage2()    	    { return this.percentage2;    }
    public List<Person> getPersons() 		{ return this.group;          }
    public double getTimeSpeak() 	  		{ return this.speakingTime;   }
    public void setTimeSpeak(double speakT) { this.speakingTime = speakT; }
    
    @Override
    public String toString() {
        return "Person [nombre=" + name + ", personality1=" + personality1
                + ", personality2=" + personality2 + ", percentage1=" + percentage1 + ", percentage2=" + percentage2 + "]";
    }

    @Override
    public void initialize() 
    {
    	//System.out.println("Here");
        listening = true;
        talking   = false;

        if (canStartConversation()) {
            talking   = true;
            listening = false;
        }

        if (listening) {
            passivate(); 
        } else if (talking) {
            double speakingT = calculateSpeakingTime();
            holdIn("talking", speakingT); 
        }
        super.initialize();
    }
    
    // DEVS transition methods #####################################################
    
    @Override
    public void deltext(double e, message x) 
    {
    	//System.out.println("ext");
        if(GroupCoupled.ElapsedTime > GroupCoupled.TotalTimeSim) {
        	System.out.println("----------------------------------------");
        	passivate();
        	GroupCoupled.showInteractionFrecuency();
        	GroupCoupled.showSpeakTime();
        	GroupCoupled.showSpeakTimeRate();
        	GroupCoupled.restoreMetrics(this.group);
        }
        else {
	    	//System.out.println(this.name+" deltext()");
	        Continue(e);
	        if (x.getLength() > 0) {
	        	double speakingT = calculateSpeakingTime();
	            talking   = true;
	            listening = false;
	            holdIn("talking", speakingT);
	        }
        }
    }
    
    @Override
    public void deltint() 
    {
    	//System.out.println("int");
        double speakingT = calculateSpeakingTime();
        //System.out.println(this.name + ", Tiempo hablado: " + speakingT);
        //String csv_record = this.name + "," + speakingT + "\n";
        //csvWriter.writeToCSV(csv_record);

        if (talking) {
            accumSpokenTime += speakingT;
            if (accumSpokenTime >= sigma) {
            	//System.out.println("Sigma: "+ sigma + ", " + this.name);
                talking          = false;
                listening        = true;
                generatedMessage = false;
                passivate();
            }
        } else {
            listening = true;
        }
    }
    
    @Override
    public message out() 
    {
    	//System.out.println("out");
        message mssge 				   = new message();
        List<String> compatiblePeoples = evaluateNextPersons();
        String nextSpeakerName         = getRandomPersonName(getBestCompatibleNames(compatiblePeoples));
        //System.out.println("List: " + compatiblePeoples);
        //System.out.println("Choose: " + nextSpeakerName);
        if (talking && !generatedMessage && !nextSpeakerName.isEmpty()) 
        {
            double speakingT = calculateSpeakingTime();
            GroupCoupled.accumulateSpeakingTimeHz(this.name, speakingT);
            GroupCoupled.addSpeakingTime(this.name, speakingT);
            GroupCoupled.ElapsedTime += speakingT;
            
            if (!nextSpeakerName.isEmpty()) {
                String outPort = "Outport " + nextSpeakerName;
                mssge.add(makeContent(outPort, new entity("Take your turn to speak")));
                generatedMessage = true;
                
                GroupCoupled.addComunicationHz(this.name, nextSpeakerName, 1); // FRECUENCY!.
                //showFrecuency();
            }   
        }
        return mssge;
    }
    
    // Helper methods ###############################################################
    
	private boolean canStartConversation() 
	{
        double highestPercentage = 0.0;
        //LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        //String rutaArchivo = "Personas.json";
        //lectorJSON.leerArchivoJSON(rutaArchivo);
        //List<Archivo> archivos = lectorJSON.getArchivos();
        
        //System.out.println("Personas " + getName() + ": " + getPersons().toString());

        for (Person person : this.group) {
            String personality1 = person.getPersonality1();
            String personality2 = person.getPersonality2();

            if (("Cohesionador".equals(personality1) || "Cohesionador".equals(personality2) ||
                 "Coordinador".equals(personality1)  || "Coordinador".equals(personality2)  ||
                 "Impulsor".equals(personality1)     || "Impulsor".equals(personality2))) {
                
                double percentage1 = person.getPercentage1();
                double percentage2 = person.getPercentage2();

                if (percentage1 > highestPercentage) highestPercentage = percentage1;
                if (percentage2 > highestPercentage) highestPercentage = percentage2;
            }
        }

        return (personality1.equals("Cohesionador") || personality2.equals("Cohesionador") ||
                personality1.equals("Coordinador")  || personality2.equals("Coordinador")  ||
                personality1.equals("Impulsor")     || personality2.equals("Impulsor"))    &&
                (percentage1 >= highestPercentage   || percentage2 >= highestPercentage);
    }
	
	private double calculateSpeakingTime() 
	{
	    //System.out.println("Tiempo de habla de " + this.name + ": " + this.speakingTime); // Tiempo de habla de Charlot: 36.300000000000004
	    return this.speakingTime;
	}
	
	private String getRandomPersonName(List<String> names) 
	{
	    if (names.isEmpty()) return "";

	    Random random = new Random();
	    int index     = random.nextInt(names.size());
	    while (names.get(index).equals(this.name)) {
	        index = random.nextInt(names.size());
	    }
	    return names.get(index);
	}
	
	private List<String> evaluateNextPersons() // TOOODOOOO :C !!!!!!
	{
	    Map<String, Double> compatibilities = new HashMap<>();
	    compatibilities.put("Investigador de Recursos-Investigador de Recursos", 1.0);
	    compatibilities.put("Investigador de Recursos-Cohesionador", 1.0);
	    compatibilities.put("Investigador de Recursos-Coordinador", 1.0);
	    compatibilities.put("Investigador de Recursos-Cerebro", 1.0);
	    compatibilities.put("Investigador de Recursos-Monitor Evaluador", 1.0);
	    compatibilities.put("Investigador de Recursos-Especialista", 1.0);
	    compatibilities.put("Investigador de Recursos-Impulsor", 1.0);
	    compatibilities.put("Investigador de Recursos-Implementador", 1.0);
	    compatibilities.put("Investigador de Recursos-Finalizador ", 1.0);
	    compatibilities.put("Cohesionador-Investigador de Recursos", 1.0);
	    compatibilities.put("Cohesionador-Cohesionador", 1.0);
	    compatibilities.put("Cohesionador-Coordinador", 1.0);
	    compatibilities.put("Cohesionador-Cerebro", 1.0);
	    compatibilities.put("Cohesionador-Monitor Evaluador", 1.0);
	    compatibilities.put("Cohesionador-Especialista", 1.0);
	    compatibilities.put("Cohesionador-Impulsor", 1.0);
	    compatibilities.put("Cohesionador-Implementador", 1.0);
	    compatibilities.put("Cohesionador-Finalizador ", 1.0);
	    compatibilities.put("Coordinador-Investigador de Recursos", 1.0);
	    compatibilities.put("Coordinador-Cohesionador", 1.0);
	    compatibilities.put("Coordinador-Coordinador", 1.0);
	    compatibilities.put("Coordinador-Cerebro", 1.0);
	    compatibilities.put("Coordinador-Monitor Evaluador", 1.0);
	    compatibilities.put("Coordinador-Especialista", 1.0);
	    compatibilities.put("Coordinador-Impulsor", 1.0);
	    compatibilities.put("Coordinador-Implementador", 1.0);
	    compatibilities.put("Coordinador-Finalizador ", 1.0);
	    compatibilities.put("Cerebro-Investigador de Recursos", 1.0);
	    compatibilities.put("Cerebro-Cohesionador", 1.0);
	    compatibilities.put("Cerebro-Coordinador", 1.0);
	    compatibilities.put("Cerebro-Cerebro", 1.0);
	    compatibilities.put("Cerebro-Monitor Evaluador", 1.0);
	    compatibilities.put("Cerebro-Especialista", 1.0);
	    compatibilities.put("Cerebro-Impulsor", 1.0);
	    compatibilities.put("Cerebro-Implementador", 1.0);
	    compatibilities.put("Cerebro-Finalizador ", 1.0);
	    compatibilities.put("Monitor Evaluador-Investigador de Recursos", 1.0);
	    compatibilities.put("Monitor Evaluador-Cohesionador", 1.0);
	    compatibilities.put("Monitor Evaluador-Coordinador", 1.0);
	    compatibilities.put("Monitor Evaluador-Cerebro", 1.0);
	    compatibilities.put("Monitor Evaluador-Monitor Evaluador", 1.0);
	    compatibilities.put("Monitor Evaluador-Especialista", 1.0);
	    compatibilities.put("Monitor Evaluador-Impulsor", 1.0);
	    compatibilities.put("Monitor Evaluador-Implementador", 1.0);
	    compatibilities.put("Monitor Evaluador-Finalizador ", 1.0);
	    compatibilities.put("Especialista-Investigador de Recursos", 1.0);
	    compatibilities.put("Especialista-Cohesionador", 1.0);
	    compatibilities.put("Especialista-Coordinador", 1.0);
	    compatibilities.put("Especialista-Cerebro", 1.0);
	    compatibilities.put("Especialista-Monitor Evaluador", 1.0);
	    compatibilities.put("Especialista-Especialista", 1.0);
	    compatibilities.put("Especialista-Impulsor", 1.0);
	    compatibilities.put("Especialista-Implementador", 1.0);
	    compatibilities.put("Especialista-Finalizador ", 1.0);
	    compatibilities.put("Impulsor-Investigador de Recursos", 1.0);
	    compatibilities.put("Impulsor-Cohesionador", 1.0);
	    compatibilities.put("Impulsor-Coordinador", 1.0);
	    compatibilities.put("Impulsor-Cerebro", 1.0);
	    compatibilities.put("Impulsor-Monitor Evaluador", 1.0);
	    compatibilities.put("Impulsor-Especialista", 1.0);
	    compatibilities.put("Impulsor-Impulsor", 1.0);
	    compatibilities.put("Impulsor-Implementador", 1.0);
	    compatibilities.put("Impulsor-Finalizador ", 1.0);
	    compatibilities.put("Implementador-Investigador de Recursos", 1.0);
	    compatibilities.put("Implementador-Cohesionador", 1.0);
	    compatibilities.put("Implementador-Coordinador", 1.0);
	    compatibilities.put("Implementador-Cerebro", 1.0);
	    compatibilities.put("Implementador-Monitor Evaluador", 1.0);
	    compatibilities.put("Implementador-Especialista", 1.0);
	    compatibilities.put("Implementador-Impulsor", 1.0);
	    compatibilities.put("Implementador-Implementador", 1.0);
	    compatibilities.put("Implementador-Finalizador ", 1.0);
	    compatibilities.put("Finalizador-Investigador de Recursos", 1.0);
	    compatibilities.put("Finalizador-Cohesionador", 1.0);
	    compatibilities.put("Finalizador-Coordinador", 1.0);
	    compatibilities.put("Finalizador-Cerebro", 1.0);
	    compatibilities.put("Finalizador-Monitor Evaluador", 1.0);
	    compatibilities.put("Finalizador-Especialista", 1.0);
	    compatibilities.put("Finalizador-Impulsor", 1.0);
	    compatibilities.put("Finalizador-Implementador", 1.0);
	    compatibilities.put("Finalizador-Finalizador ", 1.0);

	    //LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
	    //String rutaArchivo = "Personas.json";
	    //lectorJSON.leerArchivoJSON(rutaArchivo);
	    //List<Archivo> archivos = lectorJSON.getArchivos();

	    List<String> nameCompatiblePeoples = new ArrayList<>();
	    for (final Person person : this.group) 
	    {
	        String personName = person.getName();
	        if (personName.equals(this.name)) continue; // Skip the same person.
	        
	        String personality1 = person.getPersonality1();
	        String personality2 = person.getPersonality2();
	        double percentage1  = person.getPercentage1();
	        double percentage2  = person.getPercentage2();
	        String combination1 = this.personality1 + "-" + personality1;
	        String combination2 = this.personality2 + "-" + personality2;
	        
	        double totalCompatibility = 0.0;
	        if (compatibilities.containsKey(combination1)) {
	            double compatibility1 = compatibilities.get(combination1);
	            totalCompatibility += compatibility1 * percentage1 * this.percentage1;
	        }
	        if (compatibilities.containsKey(combination2)) {
	            double compatibility2 = compatibilities.get(combination2);
	            totalCompatibility += compatibility2 * percentage2 * this.percentage2;
	        }
	        nameCompatiblePeoples.add(personName + ": " + totalCompatibility);
	    }

	    nameCompatiblePeoples.sort((a, b) -> {
	        double compatibilityA = Double.parseDouble(a.split(": ")[1]);
	        double compatibilityB = Double.parseDouble(b.split(": ")[1]);
	        return Double.compare(compatibilityB, compatibilityA);
	    });
	    
	    int groupSize = GroupCoupled.GroupSize;
	    if (nameCompatiblePeoples.size() > groupSize-1) {
	        nameCompatiblePeoples = nameCompatiblePeoples.subList(0, groupSize-2);
	    }
	    return nameCompatiblePeoples;
	}
	
	private List<String> getBestCompatibleNames(List<String> bestCompatibilities) {
	    List<String> bestCompatibleNames = new ArrayList<>();

	    for (String element : bestCompatibilities) {
	        String[] partes = element.split(":");
	        
	        if (partes.length == 2) {
	        	//System.out.println("partes: " + partes[0] + " " + partes[1]);
	            String personName = partes[0].trim(); 
	            bestCompatibleNames.add(personName);
	        }
	    }
	    return bestCompatibleNames;
	}
}