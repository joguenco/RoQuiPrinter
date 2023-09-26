package dev.mestizos.serialize;

import ec.gob.sri.withhold.v200.ComprobanteRetencion;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class Withhold {

    private final String pathXmlFile;

    public Withhold(String pathXmlFile) {
        this.pathXmlFile = pathXmlFile;
    }

    public ComprobanteRetencion xmlToObject() {
        File file = new File(pathXmlFile);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ComprobanteRetencion.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return (ComprobanteRetencion) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
