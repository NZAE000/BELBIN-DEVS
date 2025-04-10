package MyPackage;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
    private String filePath;
    private FileWriter writer;

    public CSVWriter(String filePath) {
        this.filePath = filePath;
        try {
            // Establece el segundo argumento de FileWriter como `true` para habilitar la escritura en modo "append" (continuar desde donde se quedó)
            writer = new FileWriter(filePath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCSV(String registro) {
        try {
            writer.append(registro);
            writer.flush(); // Asegúrate de que los datos se escriban inmediatamente en el archivo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close(); // Cierra el archivo cuando ya no lo necesites
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
