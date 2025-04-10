package MyPackage;
import view.modeling.ViewableDigraph;

import java.util.ArrayList;
import java.util.List;

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
        String rutaArchivo = "Personas.JSON";
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
