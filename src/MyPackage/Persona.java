package MyPackage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.modeling.message;
import view.modeling.ViewableAtomic;
import GenCol.*;



public class Persona extends ViewableAtomic {
	
	public class SpeakTime{
		
		SpeakTime(String nm, Double t_e){
				this.name = nm;
				this.time_elapse = t_e;
		}
		
		public String name;
		Double time_elapse;
	}

	private static double tiempoTranscurrido = 0.0;
	private static final double total_time_simulation = 3000;
	public static int limit_to = 0;
	public static HashMap<String, Map<String, Integer>> frecuencies = new HashMap<>();
	private static HashMap<String, Double> speak_time_accumulator = new HashMap<>();
	private static ArrayList<SpeakTime> speaks_time = new ArrayList<>();
	private boolean escuchando;
    private boolean hablando;
    private String Nombre;
    private String Personalidad1;
    private String Personalidad2;
    private double Porcentaje1;
    private double Porcentaje2;
    private int numPersonas; 
    private List<String> personalidadesDelGrupo = new ArrayList<>();
    private List<Persona> personasEnGrupo = new ArrayList<>();
    private List<String> nombresMejoresCompatibilidades = new ArrayList<>();

    boolean mensajeGenerado = false;
    CSVWriter csvWriter = new CSVWriter("resultados.csv");
    
    public Persona(String Nombre, String Personalidad1, String Personalidad2, double Porcentaje1, double Porcentaje2, List<String> personalidadesDelGrupo, List<Persona> personasEnGrupo) {
        super(Nombre);

        this.Nombre = Nombre;
        this.Personalidad1 = Personalidad1;
        this.Personalidad2 = Personalidad2;
        this.Porcentaje1 = Porcentaje1;
        this.Porcentaje2 = Porcentaje2;
        this.personalidadesDelGrupo = personalidadesDelGrupo;
        this.personasEnGrupo = personasEnGrupo;

        LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        String rutaArchivo = "Personas.json";
        lectorJSON.leerArchivoJSON(rutaArchivo);
        List<Archivo> archivos = lectorJSON.getArchivos();
        this.numPersonas = archivos.size();
        
        for (Archivo archivo : archivos) {
            String nombrePersona = archivo.getNombre();

            if (!nombrePersona.equals(Nombre)) {
                addInport("Inport " + nombrePersona);
                addOutport("Outport " + nombrePersona);
            }
        }
    }
    private double tiempoHabladoAcumulado = 0.0;

    @Override
    public void initialize() {
    	
        escuchando = true;
        hablando = false;

        if (puedeIniciarConversacion()) {
            hablando = true;
            escuchando = false;
        }

        if (escuchando) {
            passivate(); 
        } else if (hablando) {
            double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
            holdIn("Hablando", tiempoDeHabla); 
        }
        super.initialize();
    }
    
    @Override
    public void deltint() {
        //System.out.println(this.Nombre + " deltint()");
        double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
        //System.out.println(this.Nombre + ", Tiempo hablado: " + tiempoDeHabla);
        String registroCSV = this.Nombre + "," + tiempoDeHabla + "\n";
        csvWriter.writeToCSV(registroCSV);

        if (hablando) {
            tiempoHabladoAcumulado += tiempoDeHabla;
            if (tiempoHabladoAcumulado >= sigma) {
                hablando = false;
                escuchando = true;
                mensajeGenerado = false;
                passivate();
            }
        } else {
            escuchando = true;
        }
    }
    @Override
    public message out() {
    	//System.out.println("\nName from: " + this.name);
        message m = new message();
        List<String> personasCompatibles = evaluarsiguientepersona(this.Nombre, limit_to, this.Personalidad1,this.Personalidad2,this.Porcentaje1,this.Porcentaje2);
        String nombreCompanero = obtenerNombrePersonaAleatoria(obtenerNombresMejoresCompatibilidades(personasCompatibles));
        //System.out.println("List: " + personasCompatibles);
        //System.out.println("Choose: " + nombreCompanero);
        if (hablando && !mensajeGenerado && !nombreCompanero.isEmpty()) {
            double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
            compute_speaking_time_hz(this.Nombre, tiempoDeHabla);
            add_speaking_time(new SpeakTime(this.Nombre, tiempoDeHabla));
            tiempoTranscurrido += tiempoDeHabla;
            if (!nombreCompanero.isEmpty()) {
                String puertoSalida = "Outport " + nombreCompanero;
                m.add(makeContent(puertoSalida, new entity("Toma tu turno de hablar")));
                mensajeGenerado = true;
                
                // FRECUENCY!
                compute_comunication_hz(this.Nombre, nombreCompanero);
                //showFrecuency();
            }
            
        }

        return m;
    }
    
    private void add_speaking_time(SpeakTime speaktime)
    {
    	//if (!speak_time_accumulator.containsKey(from))
    	//	speak_time_accumulator.put(from, 0.0);
    	//else {
    		//speak_time_accumulator.put(from, speak_time_accumulator.getOrDefault(from, 0.0) + speak_time_lapse);
    	//}
    	
    	speaks_time.add(speaktime);
    	
    }
    
    private void compute_speaking_time_hz(String from, Double speak_time_lapse)
    {

    	speak_time_accumulator.put(from, speak_time_accumulator.getOrDefault(from, 0.0) + speak_time_lapse);
    }
    
    private void compute_comunication_hz(String from, String to)
    {
    	if (!frecuencies.containsKey(from))
    		frecuencies.put(from, new HashMap<>());
        
        // Get frecuency map 'from'
        Map<String, Integer> frecuency = frecuencies.get(from);
        
        // There is no key 'to' in the map, initialize to 0
        frecuency.put(to, frecuency.getOrDefault(to, 0) + 1);
    }
    
    private void showFrecuency()
    {
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
    }
    
    private void show_speak_timeFrecuency() 
    {
    	 if(tiempoTranscurrido > total_time_simulation) {
    		 System.out.println("Name\tSpeaking percentage");
    		 for (Map.Entry<String, Double> entry : speak_time_accumulator.entrySet()) 
    		 {
    			 String speaker_name = entry.getKey();
    			 Double total_time_speaking = entry.getValue();
    			 Double percentage = total_time_speaking / total_time_simulation;
    			 System.out.println(speaker_name + "\t" + percentage + "\n");
    		 }
    	 }
    }
    
    private void show_speak_time() 
    {
    	 if(tiempoTranscurrido > total_time_simulation) {
    		 System.out.println("\nName\tSpeaking time");
    		 //for (Map.Entry<String, Double> entry : speak_time_accumulator.entrySet()) 
    		 //{
    		//	 String speaker_name = entry.getKey();
    		//	 Double total_time_speaking = entry.getValue();
    		//	 Double percentage = total_time_speaking / total_time_simulation;
    		//	 System.out.println(speaker_name + "\t" + percentage + "\n");
    		 //}
    		 Double total_time = 0.0;
    		 for (int i = 0; i < speaks_time.size(); i++) {
    			 System.out.println(speaks_time.get(i).name + "\t" + speaks_time.get(i).time_elapse + "\n");
    			 total_time += speaks_time.get(i).time_elapse;
			 }
    		 System.out.println("Total\t" + total_time + "\n");
    		 //speaks_time.forEach( (speaktime) -> { System.out.println(speaktime.name + "\t" + speaktime.time_elapse + "\n"); } );
    	 }
    }

    @Override
    public void deltext(double e, message x) {
        if(tiempoTranscurrido > total_time_simulation) {
        	System.out.println("----------------------------------------");
        	passivate();
        	showFrecuency();
        	show_speak_time();
        	show_speak_timeFrecuency();
        	tiempoTranscurrido=0.0;
        }
        else {
	    	//System.out.println(this.Nombre+" deltext()");
	        Continue(e);
	        if (x.getLength() > 0) {
	        	double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
	            hablando = true;
	            escuchando = false;
	            holdIn("Hablando", tiempoDeHabla);
	        }
        }
    }
    
	public boolean puedeIniciarConversacion() {
        double mayorPorcentaje = 0.0;
        LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        String rutaArchivo = "Personas.json";
        lectorJSON.leerArchivoJSON(rutaArchivo);
        List<Archivo> archivos = lectorJSON.getArchivos();

        for (Archivo archivo : archivos) {
            String personalidad1 = archivo.getPersonalidad1();
            String personalidad2 = archivo.getPersonalidad2();

            if (("Cohesionador".equals(personalidad1) || "Cohesionador".equals(personalidad2) ||
                 "Coordinador".equals(personalidad1) || "Coordinador".equals(personalidad2) ||
                 "Impulsor".equals(personalidad1) || "Impulsor".equals(personalidad2))) {
                
                double porcentaje = archivo.getPorcentaje1();
                double porcentaje2 = archivo.getPorcentaje2();

                if (porcentaje > mayorPorcentaje) {
                    mayorPorcentaje = porcentaje;
                }

                if (porcentaje2 > mayorPorcentaje) {
                    mayorPorcentaje = porcentaje2;
                }
            }
        }

        return (Personalidad1.equals("Cohesionador") || Personalidad2.equals("Cohesionador") ||
                Personalidad1.equals("Coordinador") || Personalidad2.equals("Coordinador") ||
                Personalidad1.equals("Impulsor") || Personalidad2.equals("Impulsor")) &&
                (Porcentaje1 >= mayorPorcentaje || Porcentaje2 >= mayorPorcentaje);
    }
	public double calcularTiempoDeHabla(List<String> personalidadesDelGrupo) {

	    Map<String, Double> tiemposDeHablaAdicionales = new HashMap<>();

	    // Combinaciones de personalidades
	    
	    // 1
	    /*
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Finalizador ", 3.0);
    	tiemposDeHablaAdicionales.put("Cohesionador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Coordinador", 4.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Finalizador ", 3.0);
	   	tiemposDeHablaAdicionales.put("Especialista-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Especialista-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Implementador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Finalizador ", 3.0);
	    //*/
	    
	    // 2
	    /*
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Finalizador ", 3.0);
    	tiemposDeHablaAdicionales.put("Cohesionador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Coordinador", 4.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Finalizador ", 3.0);
	   	tiemposDeHablaAdicionales.put("Especialista-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Especialista-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Implementador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Finalizador ", 3.0);
	    //*/
	    
	    // 3
	    /*
	 	tiemposDeHablaAdicionales.put("Investigador de Recursos-Investigador de Recursos", 1.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Finalizador ", 3.0);
    	tiemposDeHablaAdicionales.put("Cohesionador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Coordinador", 4.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Finalizador ", 3.0);
	   	tiemposDeHablaAdicionales.put("Especialista-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Especialista-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Implementador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Impulsor", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Implementador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Finalizador ", 4.0); 
	    //*/
	    
	    // 4
	    ///*
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Investigador de Recursos", 1.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Finalizador ", 3.0);
    	tiemposDeHablaAdicionales.put("Cohesionador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Coordinador", 4.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Finalizador ", 3.0);
	   	tiemposDeHablaAdicionales.put("Especialista-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Especialista-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Implementador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Impulsor", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Implementador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Finalizador ", 4.0);
	    //*/

	    double tiempoDeHabla = 0.0;
	    double promedioPorcentajes = (Porcentaje1 + Porcentaje2) / 2.0;
	    

	    for (String personalidadGrupo : personalidadesDelGrupo) {
	        String combinacion1 = Personalidad1 + "-" + personalidadGrupo;
	        String combinacion2 = Personalidad2 + "-" + personalidadGrupo;

	        if (tiemposDeHablaAdicionales.containsKey(combinacion1)) {
	            tiempoDeHabla += tiemposDeHablaAdicionales.get(combinacion1);
	        }
	        if (tiemposDeHablaAdicionales.containsKey(combinacion2)) {
	            tiempoDeHabla += tiemposDeHablaAdicionales.get(combinacion2);
	        }
	    }
	    tiempoDeHabla=tiempoDeHabla*promedioPorcentajes;
	    return tiempoDeHabla;
	}
	private String obtenerNombrePersonaAleatoria(List<String> listaNombres) {
	    if (listaNombres.isEmpty()) {
	        return "";
	    }

	    Random random = new Random();
	    int index = random.nextInt(listaNombres.size());

	    while (listaNombres.get(index).equals(Nombre)) {
	        index = random.nextInt(listaNombres.size());
	    }

	    return listaNombres.get(index);
	}
	public List<String> evaluarsiguientepersona(String personaQueHabla, int limite, String per1,String per2 ,double P1,double P2) {
	    Map<String, Double> Compatibilidad = new HashMap<>();
	    Compatibilidad.put("Investigador de Recursos-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Cohesionador", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Coordinador", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Cerebro", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Especialista", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Impulsor", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Implementador", 1.0);
	    Compatibilidad.put("Investigador de Recursos-Finalizador ", 1.0);
	    Compatibilidad.put("Cohesionador-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Cohesionador-Cohesionador", 1.0);
	    Compatibilidad.put("Cohesionador-Coordinador", 1.0);
	    Compatibilidad.put("Cohesionador-Cerebro", 1.0);
	    Compatibilidad.put("Cohesionador-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Cohesionador-Especialista", 1.0);
	    Compatibilidad.put("Cohesionador-Impulsor", 1.0);
	    Compatibilidad.put("Cohesionador-Implementador", 1.0);
	    Compatibilidad.put("Cohesionador-Finalizador ", 1.0);
	    Compatibilidad.put("Coordinador-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Coordinador-Cohesionador", 1.0);
	    Compatibilidad.put("Coordinador-Coordinador", 1.0);
	    Compatibilidad.put("Coordinador-Cerebro", 1.0);
	    Compatibilidad.put("Coordinador-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Coordinador-Especialista", 1.0);
	    Compatibilidad.put("Coordinador-Impulsor", 1.0);
	    Compatibilidad.put("Coordinador-Implementador", 1.0);
	    Compatibilidad.put("Coordinador-Finalizador ", 1.0);
	    Compatibilidad.put("Cerebro-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Cerebro-Cohesionador", 1.0);
	    Compatibilidad.put("Cerebro-Coordinador", 1.0);
	    Compatibilidad.put("Cerebro-Cerebro", 1.0);
	    Compatibilidad.put("Cerebro-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Cerebro-Especialista", 1.0);
	    Compatibilidad.put("Cerebro-Impulsor", 1.0);
	    Compatibilidad.put("Cerebro-Implementador", 1.0);
	    Compatibilidad.put("Cerebro-Finalizador ", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Cohesionador", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Coordinador", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Cerebro", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Especialista", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Impulsor", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Implementador", 1.0);
	    Compatibilidad.put("Monitor Evaluador-Finalizador ", 1.0);
	    Compatibilidad.put("Especialista-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Especialista-Cohesionador", 1.0);
	    Compatibilidad.put("Especialista-Coordinador", 1.0);
	    Compatibilidad.put("Especialista-Cerebro", 1.0);
	    Compatibilidad.put("Especialista-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Especialista-Especialista", 1.0);
	    Compatibilidad.put("Especialista-Impulsor", 1.0);
	    Compatibilidad.put("Especialista-Implementador", 1.0);
	    Compatibilidad.put("Especialista-Finalizador ", 1.0);
	    Compatibilidad.put("Impulsor-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Impulsor-Cohesionador", 1.0);
	    Compatibilidad.put("Impulsor-Coordinador", 1.0);
	    Compatibilidad.put("Impulsor-Cerebro", 1.0);
	    Compatibilidad.put("Impulsor-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Impulsor-Especialista", 1.0);
	    Compatibilidad.put("Impulsor-Impulsor", 1.0);
	    Compatibilidad.put("Impulsor-Implementador", 1.0);
	    Compatibilidad.put("Impulsor-Finalizador ", 1.0);
	    Compatibilidad.put("Implementador-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Implementador-Cohesionador", 1.0);
	    Compatibilidad.put("Implementador-Coordinador", 1.0);
	    Compatibilidad.put("Implementador-Cerebro", 1.0);
	    Compatibilidad.put("Implementador-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Implementador-Especialista", 1.0);
	    Compatibilidad.put("Implementador-Impulsor", 1.0);
	    Compatibilidad.put("Implementador-Implementador", 1.0);
	    Compatibilidad.put("Implementador-Finalizador ", 1.0);
	    Compatibilidad.put("Finalizador-Investigador de Recursos", 1.0);
	    Compatibilidad.put("Finalizador-Cohesionador", 1.0);
	    Compatibilidad.put("Finalizador-Coordinador", 1.0);
	    Compatibilidad.put("Finalizador-Cerebro", 1.0);
	    Compatibilidad.put("Finalizador-Monitor Evaluador", 1.0);
	    Compatibilidad.put("Finalizador-Especialista", 1.0);
	    Compatibilidad.put("Finalizador-Impulsor", 1.0);
	    Compatibilidad.put("Finalizador-Implementador", 1.0);
	    Compatibilidad.put("Finalizador-Finalizador ", 1.0);


	    LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
	    String rutaArchivo = "Personas.json";
	    lectorJSON.leerArchivoJSON(rutaArchivo);
	    List<Archivo> archivos = lectorJSON.getArchivos();


	    List<String> personasConCompatibilidad = new ArrayList<>();

	    for (Archivo archivo : archivos) {
	        String nombrePersona = archivo.getNombre();
	        if (nombrePersona.equals(personaQueHabla)) {
	            continue;
	        }
	        
	        String personalidadPersona1 = archivo.getPersonalidad1();
	        String personalidadPersona2 = archivo.getPersonalidad2();
	        double porcentajePersona1 = archivo.getPorcentaje1();
	        double porcentajePersona2 = archivo.getPorcentaje2();
	        
	        double compatibilidadTotal = 0.0;
	        String combinacion1 =  per1+ "-" + personalidadPersona1;
	        String combinacion2 =  per2+ "-" + personalidadPersona2;
	        if (Compatibilidad.containsKey(combinacion1)) {
	            double compatibilidad1 = Compatibilidad.get(combinacion1);
	            compatibilidadTotal += compatibilidad1 * porcentajePersona1 * P1;
	        }
	        if (Compatibilidad.containsKey(combinacion2)) {
	            double compatibilidad2 = Compatibilidad.get(combinacion2);
	            compatibilidadTotal += compatibilidad2 * porcentajePersona2 * P2;
	        }
	        
	        personasConCompatibilidad.add(nombrePersona + ": " + compatibilidadTotal);
	    }

	    personasConCompatibilidad.sort((a, b) -> {
	        double compatibilidadA = Double.parseDouble(a.split(": ")[1]);
	        double compatibilidadB = Double.parseDouble(b.split(": ")[1]);
	        return Double.compare(compatibilidadB, compatibilidadA);
	    });


	    if (personasConCompatibilidad.size() > limite) {
	        personasConCompatibilidad = personasConCompatibilidad.subList(0, limite-1);
	    }

	    return personasConCompatibilidad;
	}
	public List<String> obtenerNombresMejoresCompatibilidades(List<String> mejoresCompatibilidades) {
	    List<String> nombresMejoresCompatibilidades = new ArrayList<>();

	    for (String elemento : mejoresCompatibilidades) {
	        String[] partes = elemento.split(":");
	        if (partes.length == 2) {
	            String nombrePersona = partes[0].trim(); 
	            nombresMejoresCompatibilidades.add(nombrePersona);
	        }
	    }

	    return nombresMejoresCompatibilidades;
	}
	
}