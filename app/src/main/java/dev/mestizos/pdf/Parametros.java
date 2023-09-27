package dev.mestizos.pdf;

import ec.gob.sri.invoice.v210.InfoTributaria;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parametros {

    String direccionReportes;
    String direccionLogoJpeg;

    public Parametros(String direccionReportes, String direccionLogoJpeg) {
        /*
        Ejemplo:
        resources/images/logo.jpeg
        */
        this.direccionReportes = direccionReportes;
        this.direccionLogoJpeg = direccionLogoJpeg;
    }

    public Map<String, Object> obtenerParametrosInfoTriobutaria(InfoTributaria infoTributaria, String numAut, String fechaAut) {
        Map param = new HashMap();
        param.put("RUC", infoTributaria.getRuc());
        param.put("CLAVE_ACC", infoTributaria.getClaveAcceso());
        param.put("RAZON_SOCIAL", infoTributaria.getRazonSocial());
        param.put("DIR_MATRIZ", infoTributaria.getDirMatriz());
        param.put("AGENTE_RETENCION", infoTributaria.getAgenteRetencion());
        param.put("CONTRIBUYENTE_RIMPE", infoTributaria.getContribuyenteRimpe());
        try {
            param.put("LOGO", new FileInputStream(this.direccionLogoJpeg));
//            param.put("LOGO", new FileInputStream("resources/images/logo.jpeg"));

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Parametros.class.getName()).log(Level.SEVERE, null, ex);
        }
//        param.put("SUBREPORT_DIR", "./resources/reportes/");

        param.put("SUBREPORT_DIR", this.direccionReportes + File.separator);
        param.put("SUBREPORT_PAGOS", this.direccionReportes + File.separator);
        param.put("SUBREPORT_TOTALES", this.direccionReportes + File.separator);
        if (infoTributaria.getTipoEmision().equals("1")) {
            param.put("TIPO_EMISION", "Normal");
        } else {
            param.put("TIPO_EMISION", "Indisponibilidad del Sistema");
        }
        param.put("NUM_AUT", numAut);
        param.put("FECHA_AUT", fechaAut);
        param.put("MARCA_AGUA", "");
        param.put("NUM_FACT", infoTributaria.getEstab() + "-" + infoTributaria.getPtoEmi() + "-" + infoTributaria.getSecuencial());
        if (infoTributaria.getAmbiente().equals("1")) {
            param.put("AMBIENTE", "Pruebas");
        } else {
            param.put("AMBIENTE", "Producción");
        }
        param.put("NOM_COMERCIAL", infoTributaria.getNombreComercial());
        return param;
    }
}
