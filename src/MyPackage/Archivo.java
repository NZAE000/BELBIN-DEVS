package MyPackage;

public class Archivo {
    private String nombre;
    private String codigo;
    private String personalidad1;
    private String personalidad2;
    private double porcentaje1;
    private double porcentaje2;

    public Archivo(String nombre, String codigo, String personalidad1, String personalidad2, double porcentaje1, double porcentaje2) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.personalidad1 = personalidad1;
        this.personalidad2 = personalidad2;
        this.porcentaje1 = porcentaje1;
        this.porcentaje2 = porcentaje2;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getPersonalidad1() {
        return personalidad1;
    }

    public String getPersonalidad2() {
        return personalidad2;
    }

    public double getPorcentaje1() {
        return porcentaje1;
    }

    public double getPorcentaje2() {
        return porcentaje2;
    }

    @Override
    public String toString() {
        return "Archivo [nombre=" + nombre + ", codigo=" + codigo + ", personalidad1=" + personalidad1
                + ", personalidad2=" + personalidad2 + ", porcentaje1=" + porcentaje1 + ", porcentaje2=" + porcentaje2 + "]";
    }
}