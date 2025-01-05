package dev.joguenco.serialize;

import ec.gob.sri.liquidation.v110.LiquidacionCompra;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class Liquidation {

    private final String pathXmlFile;

    public Liquidation(String pathXmlFile) {
        this.pathXmlFile = pathXmlFile;
    }

    public LiquidacionCompra xmlToObject() {
        File file = new File(pathXmlFile);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LiquidacionCompra.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return (LiquidacionCompra) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
