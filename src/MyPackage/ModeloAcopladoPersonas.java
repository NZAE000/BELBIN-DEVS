package MyPackage;
import view.modeling.ViewableDigraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeloAcopladoPersonas extends ViewableDigraph {
	
    private int numPersonas 					 = 0;
    private List<String>  personalidadesDelGrupo =  new ArrayList<>();
    private List<Persona> personas               =  new ArrayList<>();
    private List<List<Object>> personal_data;
    
    public ModeloAcopladoPersonas() {
        super("ModeloAcopladoPersonas");
        initializeModel();
    }

    private void initializeModel() 
    {
    	// Read json.
    	JSONReader jsonreader = new JSONReader();
    	personal_data = jsonreader.read(
		    "Personas.json",
		    Arrays.asList("nombre", "personalidad1", "personalidad2", "porcentaje1", "porcentaje2"),
		    Arrays.asList(String.class, String.class, String.class, Double.class, Double.class)
		);
        numPersonas = personal_data.size();
        
        // Init all person atomic models.
        for (int i = 0; i < numPersonas; i++) {
        	List<Object> data   = personal_data.get(i);
        	String name         = data.get(0).toString();
        	String personality1 = data.get(1).toString();
        	String personality2 = data.get(2).toString();
        	double percentaje1  = Double.parseDouble(data.get(3).toString());
        	double percentaje2  = Double.parseDouble(data.get(4).toString());
            personas.add(new Persona(
            	name,
            	personality1,
            	personality2,
            	percentaje1,
            	percentaje2,
                personalidadesDelGrupo,
                personas
            ));
            add(personas.get(i));
            
            // Add the personalities if are not.
            if (!personalidadesDelGrupo.contains(personality1))
            	 personalidadesDelGrupo.add(personality1);
            if (!personalidadesDelGrupo.contains(personality2))
            	personalidadesDelGrupo.add(personality2);
        }
        
        
        // Set in/out ports for all person atomic models.
        for (Persona persona1 : personas) {
        	for (Persona persona2 : personas) {
                String nombrePersona = persona2.getName();
                if (!nombrePersona.equals(persona1.getName())) {
                	persona1.addInport("Inport " + nombrePersona);
                	persona1.addOutport("Outport " + nombrePersona);
                }
            }
        }
        
        // Show all group personality and persons.
        for (int i = 0; i < numPersonas; i++) {
        	System.out.println("Personalidades persona " + personas.get(i).getName() + ": " + personas.get(i).getPersonalidadesDelGrupo().toString());
        	System.out.println("Personas " + personas.get(i).getName() + ": " + personas.get(i).getPersons().toString());
        }
        
        // Fill matrix frecuency.
        for (int i = 0; i < numPersonas; i++) {
        	Persona.frecuencies.put(personas.get(i).getName(), new HashMap<>());
        	Map<String, Integer> frecuency = Persona.frecuencies.get(personas.get(i).getName());
        	for (int j = 0; j < numPersonas; j++) {
        		frecuency.put(personas.get(j).getName(), 0);
            }
        }
        
        Persona.limit_to = numPersonas - 1;
        
        // Show matrix frecuency.
        for (Map.Entry<String, Map<String, Integer>> entry : Persona.frecuencies.entrySet()) {
            String name = entry.getKey();
            System.out.print("\t" + name);
        }
        System.out.println();
        for (Map.Entry<String, Map<String, Integer>> entry : Persona.frecuencies.entrySet()) {
            String from = entry.getKey();
            System.out.print(from);
            Map<String, Integer> to = entry.getValue();

            for (Map.Entry<String, Integer> entryInterno : to.entrySet()) {
                //String claveInterna = entryInterno.getKey();
                Integer hz = entryInterno.getValue();
                System.out.print("\t" + hz);
            }
            System.out.println();
        }
        
        // Establish coupling.
        for (int i = 0; i < numPersonas; i++) {
            for (int k = 0; k < numPersonas; k++) {
                if (i != k) {
                    addCoupling(personas.get(i), "Outport " + personas.get(k).getName(), personas.get(k), "Inport " + personas.get(k).getName());
                }
            }
        }

        //initialize();
    }
}
