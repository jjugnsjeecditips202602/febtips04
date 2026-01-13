package io.github.futokiyo.febtips04.sample;

import io.github.futokiyo.febtips04.utils.WeightUtils;
import jakarta.enterprise.context.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

@RequestScoped
public class RqstSampleForDynamicLookup implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(RqstSampleForDynamicLookup.class);


    private final String identificationUuid;
    private final String weight;

    public RqstSampleForDynamicLookup(){
        this.identificationUuid = UUID.randomUUID().toString();
        this.weight = WeightUtils.generateWeight(100);
        logger.info("called RqstSampleForDynamicLookup constructor idUuid:{}", this.identificationUuid);
    }

    public String getIdentificationUuid(){
        return this.identificationUuid;
    }

    public String getWeight(){
        return this.weight;
    }
}
