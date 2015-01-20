package com.logginghub.logging.frontend.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD) public class ParserBlockConfiguration {

    @XmlElementWrapper(name = "raw") @XmlElement(name = "parser") private List<ParserConfiguration> rawParsers = new ArrayList<ParserConfiguration>();
    @XmlElement(name = "chunker") private List<ChunkerConfiguration> chunkingParsers = new ArrayList<ChunkerConfiguration>();

    public List<ChunkerConfiguration> getChunkingParsers() {
        return chunkingParsers;
    }

    public List<ParserConfiguration> getRawParsers() {
        return rawParsers;
    }
}
