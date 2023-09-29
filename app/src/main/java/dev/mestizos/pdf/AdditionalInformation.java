package dev.mestizos.pdf;

import lombok.Getter;
import lombok.Setter;

public class AdditionalInformation
{
    @Getter
    @Setter
    private String valor;

    @Getter
    @Setter
    private String nombre;

    public AdditionalInformation(String valor, String nombre)
    {
        this.valor = valor;
        this.nombre = nombre;
    }
}