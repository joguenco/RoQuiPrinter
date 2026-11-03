package dev.joguenco.pdf;

import ec.gob.sri.InfoTributaria;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Information {

    public static Map<String, Object> getParameters(
            InfoTributaria infoTributaria,
            String numAut,
            String fechaAut,
            String pathLogo,
            String reportFolder) {
        Map param = new HashMap();
        param.put("RUC", infoTributaria.getRuc());
        param.put("CLAVE_ACC", infoTributaria.getClaveAcceso());
        param.put("RAZON_SOCIAL", infoTributaria.getRazonSocial());
        param.put("DIR_MATRIZ", infoTributaria.getDirMatriz());
        param.put("AGENTE_RETENCION", infoTributaria.getAgenteRetencion());
        param.put("REGIMEN_RIMPE", infoTributaria.getContribuyenteRimpe());
        try {
            param.put("LOGO", new FileInputStream(pathLogo));
        } catch (FileNotFoundException ex) {
            System.out.println("Error " + ex.getMessage());
        }

        param.put("SUBREPORT_DIR", reportFolder + File.separator);
        param.put("SUBREPORT_PAGOS", reportFolder + File.separator);
        param.put("SUBREPORT_TOTALES", reportFolder + File.separator);
        if (infoTributaria.getTipoEmision().equals("1")) {
            param.put("TIPO_EMISION", "Normal");
        } else {
            param.put("TIPO_EMISION", "Indisponibilidad del Sistema");
        }
        param.put("NUM_AUT", numAut);
        param.put("FECHA_AUT", fechaAut);
        param.put("MARCA_AGUA", "");
        param.put(
                "NUM_FACT",
                infoTributaria.getEstab()
                        + "-"
                        + infoTributaria.getPtoEmi()
                        + "-"
                        + infoTributaria.getSecuencial());
        if (infoTributaria.getAmbiente().equals("1")) {
            param.put("AMBIENTE", "Pruebas");
        } else {
            param.put("AMBIENTE", "Producción");
        }
        param.put("NOM_COMERCIAL", infoTributaria.getNombreComercial());
        return param;
    }
}
