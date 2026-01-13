package io.github.futokiyo.febtips04.sample;

import io.github.futokiyo.febtips04.aop.Logging;
import io.github.futokiyo.febtips04.utils.WeightUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RequestScoped
public class RqstSampleForInjection {
    private static Logger logger = LoggerFactory.getLogger(RqstSampleForInjection.class);

    private final String identificationUuid;
    private final String weight;

    @Inject
    private DpndntSampleForRequestScopedInjection dpndntSampleForRInjcton;

    public RqstSampleForInjection(){
        this.identificationUuid = UUID.randomUUID().toString();
        this.weight = WeightUtils.generateWeight(100);
        logger.info("called RqstSampleForInjection constructor idUuid:{}", this.identificationUuid);
    }

    @Logging
    public String getIdentificationUuid(){
        return this.identificationUuid;
    }

    @Logging
    public String getWeight(){
        return this.weight;
    }

    @Logging
    public String getIdUuidOfDpndntSampleForRInjcton(){
        return this.dpndntSampleForRInjcton.getIdentificationUuid();
    }
}
