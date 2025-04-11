package MyPackage;
import view.modeling.ViewableDigraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeloAcopladoPersonas extends ViewableDigraph {
	
    private int numPersonas;
    private List<String> personalidadesDelGrupo;
    private List<Persona> personasEnGrupo;
    
    public ModeloAcopladoPersonas() {
        super("ModeloAcopladoPersonas");
        personasEnGrupo = new ArrayList<>();
        initializeModel();
    }

    private void initializeModel() {
        LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        String rutaArchivo = "Personas.json";
        lectorJSON.leerArchivoJSON(rutaArchivo);
        List<Archivo> archivos = lectorJSON.getArchivos();
        numPersonas = archivos.size();

        personalidadesDelGrupo = new ArrayList<>();
        Persona[] personas = new Persona[numPersonas];
        
        //System.out.println("Num: " + numPersonas);
        for (int i = 0; i < numPersonas; i++) {
            Archivo archivo = archivos.get(i);
            //System.out.println("Nombre: " + archivo.getNombre());
            personas[i] = new Persona(
                archivo.getNombre(),
                archivo.getPersonalidad1(),
                archivo.getPersonalidad2(),
                archivo.getPorcentaje1(),
                archivo.getPorcentaje2(),
                personalidadesDelGrupo,
                personasEnGrupo
            );
            add(personas[i]);

            personalidadesDelGrupo.add(archivo.getPersonalidad1());
            personalidadesDelGrupo.add(archivo.getPersonalidad2());
            personasEnGrupo.add(personas[i]);

        }
        
        // Fill matrix frecuency.
        for (int i = 0; i < numPersonas; i++) {
        	Persona.frecuencies.put(personas[i].getName(), new HashMap<>());
        	Map<String, Integer> frecuency = Persona.frecuencies.get(personas[i].getName());
        	for (int j = 0; j < numPersonas; j++) {
        		frecuency.put(personas[j].getName(), 0);
            }
        }
        
        Persona.limit_to = numPersonas - 1;
        
        /*for (Map.Entry<String, Map<String, Integer>> entry : Persona.frecuencies.entrySet()) {
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
        }*/
        
        
        for (int i = 0; i < numPersonas; i++) {
            for (int k = 0; k < numPersonas; k++) {
                if (i != k) {
                    addCoupling(personas[i], "Outport " + personas[k].getName(), personas[k], "Inport " + personas[i].getName());
                }
            }
        }
        initialize();
    }
}
