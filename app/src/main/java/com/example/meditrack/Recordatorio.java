package com.example.meditrack;

public class Recordatorio {
    public int codigo;
    public String medicamento;
    public String hora;
    public int cantidad;
    public int dosis;

    @Override
    public String toString() {
        return medicamento + " - " + hora + " hora/s" +
                " - (x" + dosis + ")" + " - Restantes: " + cantidad;
    }
}
