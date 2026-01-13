package io.github.futokiyo.febtips04.sample;

import io.github.futokiyo.febtips04.utils.WeightUtils;
import io.github.futokiyo.febtips04.aop.Logging;
import jakarta.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

@Dependent
public class Sample implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(Sample.class);

    private final String identificationUuid;
    private final String weight;

    public Sample(){
        this.identificationUuid = UUID.randomUUID().toString();
        this.weight = WeightUtils.generateWeight(100);
        logger.info("called DpndntSampleForDynamicLookup Sample constructor idUuid:{}", this.identificationUuid);
    }

    @Logging
    public String getIdentificationUuid(){
        return this.identificationUuid;
    }

    @Logging
    public String getWeight(){
        return this.weight;
    }
}
