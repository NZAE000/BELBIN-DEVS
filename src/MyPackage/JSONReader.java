package MyPackage;

//String rutaArchivo = "C:\\Users\\silvi\\OneDrive\\Escritorio\\DEVS_Suite_3.0.0_mixed_win64\\Personas.JSON";
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class JSONReader {

	public List<List<Object>> read(String path, List<String> fields, List<Class<?>> types) 
	{
        List<List<Object>> data = new ArrayList<>();
        try {
            FileReader fileReader   = new FileReader(path);
            JSONTokener jsonTokener = new JSONTokener(fileReader);
            JSONArray jsonArray     = new JSONArray(jsonTokener);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj   = jsonArray.getJSONObject(i);
                List<Object> row = new ArrayList<>();

                for (int j = 0; j < fields.size(); j++) {
                    String field  = fields.get(j);
                    Class<?> type = types.get(j);

                    Object valor;
                    if (type == String.class)       valor = obj.getString(field);
                    else if (type == Double.class)  valor = obj.getDouble(field);
                    else if (type == Integer.class) valor = obj.getInt(field);
                    else if (type == Boolean.class) valor = obj.getBoolean(field);
                    else throw new IllegalArgumentException("Unsupported type: " + type.getName());
                    row.add(valor);
                }
                data.add(row);
            }
            fileReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
    
}