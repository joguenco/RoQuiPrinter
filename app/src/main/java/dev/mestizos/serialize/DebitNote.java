package dev.mestizos.serialize;

import ec.gob.sri.note.debit.v100.NotaDebito;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class DebitNote {

    private final String pathXmlFile;

    public DebitNote(String pathXmlFile) {
        this.pathXmlFile = pathXmlFile;
    }

    public NotaDebito xmlToObject() {
        File file = new File(pathXmlFile);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NotaDebito.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return (NotaDebito) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
