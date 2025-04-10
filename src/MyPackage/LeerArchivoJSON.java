package MyPackage;

//String rutaArchivo = "C:\\Users\\silvi\\OneDrive\\Escritorio\\DEVS_Suite_3.0.0_mixed_win64\\Personas.JSON";
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LeerArchivoJSON {
    private List<Archivo> archivos = new ArrayList<>();

    public void leerArchivoJSON(String rutaArchivo) {
        try {
            FileReader fileReader = new FileReader(rutaArchivo);
            JSONTokener jsonTokener = new JSONTokener(fileReader);
            JSONArray jsonArray = new JSONArray(jsonTokener);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject personaJson = jsonArray.getJSONObject(i);

                String nombre = personaJson.getString("nombre");
                String codigo = personaJson.getString("codigo");
                String personalidad1 = personaJson.getString("personalidad1");
                String personalidad2 = personaJson.getString("personalidad2");
                double porcentaje1 = personaJson.getDouble("porcentaje1");
                double porcentaje2 = personaJson.getDouble("porcentaje2");

                Archivo archivo = new Archivo(nombre, codigo, personalidad1, personalidad2, porcentaje1, porcentaje2);
                archivos.add(archivo);

            }
            

            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Archivo> getArchivos() {
        return archivos;
    }
}