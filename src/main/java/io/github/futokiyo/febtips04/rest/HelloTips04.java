/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.futokiyo.febtips04.rest;


import io.github.futokiyo.febtips04.sample.DpndntSampleForApplicationScopedInjection;
import io.github.futokiyo.febtips04.sample.Sample;
import io.github.futokiyo.febtips04.sample.RqstSampleForDynamicLookup;
import io.github.futokiyo.febtips04.sample.RqstSampleForInjection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * CDI.current().select(クラスobject).get()という方法で動的に取得したBeanのメモリが解放されるかを確認する。
 *
 */

@Path("/")
@ApplicationScoped
public class HelloTips04 {

    private static Logger logger = LoggerFactory.getLogger(HelloTips04.class);

    @Inject
    private RqstSampleForInjection rqstSampleForInjection;

     @Inject
    private RqstSampleForDynamicLookup rqstSampleForDynamicLookupByInjection;

    @Inject
    private DpndntSampleForApplicationScopedInjection dpndntSampleForAppInjcton;

    @GET
    @Path("/beanlist")
    @Produces(MediaType.TEXT_HTML)
    public String getBeanList() {
        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Bean<?>> beanSet = beanManager.getBeans(Object.class, new AnnotationLiteral<Any>(){});
        Map<String, Integer> counter = new HashMap<>();
        Map<String, List<String>> scopeMap = new HashMap<>();
        for(Bean<?> bean : beanSet) {
            String fqcn = bean.getBeanClass().getCanonicalName();
            String scopeName = bean.getScope().getName().replace("jakarta.enterprise.context.", "");
            if(!counter.containsKey(fqcn)) {
                counter.put(fqcn, 1);
                List<String> list = new ArrayList<>();
                list.add(scopeName);
                scopeMap.put(fqcn, list);
            } else {
                int num = counter.get(fqcn);
                counter.put(fqcn, num+1);

                List<String> list = scopeMap.get(fqcn);
                list.add(scopeName);
                scopeMap.put(fqcn, list);
            }
        }

        StringBuilder returningSb = new StringBuilder("<html><body>");
        counter.keySet().stream().sorted().forEach(clazzNm -> {
            returningSb.append(clazzNm + " size:" + counter.get(clazzNm))
                    .append(" scope:" + String.join(",", scopeMap.get(clazzNm)))
                    .append("<br />");
        });

        returningSb.append("</body></html>");

        return returningSb.toString();
    }

    @GET
    @Path("/rqst")
    @Produces(MediaType.TEXT_HTML)
    public String getRqst() {
        RqstSampleForDynamicLookup rqstSampleForDynamicLookup = CDI.current().select(RqstSampleForDynamicLookup.class).get();
        StringBuilder returningSb = new StringBuilder("<html><body>");
        returningSb.append("<p>RequestScoped Object (CDI.current().select(RqstSampleForDynamicLookup.class).get()) rqstSampleForDynamicLookup.idUuid:")
                .append(rqstSampleForDynamicLookup.getIdentificationUuid())
                .append("</p><br />");

        if(rqstSampleForDynamicLookup==this.rqstSampleForDynamicLookupByInjection
                && rqstSampleForDynamicLookup.getIdentificationUuid().equals(this.rqstSampleForDynamicLookupByInjection.getIdentificationUuid())
                && rqstSampleForDynamicLookup.getWeight().equals(this.rqstSampleForDynamicLookupByInjection.getWeight())){
            returningSb.append("<p>rqstSampleForDynamicLookup equals rqstSampleForDynamicLookupByInjection</p>");
        }

        returningSb.append("<br />")
                .append(String.format( "<p>rqstSampleForInjection:idUuid:%1$s -> injectedDpndntSampleUuid:%2$s.</p><br />" , rqstSampleForInjection.getIdentificationUuid() , rqstSampleForInjection.getIdUuidOfDpndntSampleForRInjcton() ))
                .append("<p>DpndntSampleForApplicationScopedInjection.idUuid:")
                .append(this.dpndntSampleForAppInjcton.getIdentificationUuid())
                .append("</p><br />");

        returningSb.append(generateMemoryUsage())
                .append("<br />");
        returningSb.append("</body></html>");
        return returningSb.toString();
    }

    @GET
    @Path("/dpndnt")
    @Produces(MediaType.TEXT_HTML)
    public String getDpndnt() {
        Sample sample = CDI.current().select(Sample.class).get();

        StringBuilder returningSb = new StringBuilder("<html><body>");
        returningSb.append("<p>Dependent Object (CDI.current().select(Sample.class).get()) sample.idUuid:")
                .append(sample.getIdentificationUuid())
                .append("</p><br />")
                .append(generateMemoryUsage())
                .append("<br />");
        //destroyCDIDependentBean(sample);
        return returningSb.toString();
    }

    @GET
    @Path("/memoryusage")
    public String getMemoryUsage() {
        StringBuilder returningSb = new StringBuilder("<html><body><p>");
        returningSb.append(generateMemoryUsage());
        returningSb.append("</p></body></html>");
        return returningSb.toString();
    }

    private void destroyCDIDependentBean(Object object) {
        // Weld Proxyインターフェースを実装している場合、CDI Bean Scopeが疑似スコープ(Dependent)の場合にのみdestroy
        if (object instanceof org.jboss.weld.bean.proxy.ProxyObject) {
            BeanManager beanManager = CDI.current().getBeanManager();
            Class<?> clazz = object.getClass();
            do{
                Bean<?> spiBean = null;
                try {
                    spiBean = obtainResolvedBean(beanManager, clazz, object);
                } catch (Exception e) {
                    return ;
                }

                if (spiBean!=null) {
                    // 疑似スコープ(Dependentスコープ)の場合、destroy実行
                    if(!beanManager.isNormalScope(spiBean.getScope())) {
                        // オブジェクトを破棄
                        CDI.current().destroy(object);
                        logger.info("{}  destroyed.", object);
                    }
                    return;
                }
                clazz = clazz.getSuperclass();
                // WELD-001318 AmbiguousResolutionException 対策
                if(clazz == Object.class) {
                    logger.warn("false is returned forcely to avoid an AmbiguousResolution. object: {} , class of object:{} , clazz:{} ."
                            , object, object.getClass().getCanonicalName(), clazz.getCanonicalName() );
                    return;
                }
            } while (clazz != Object.class);
        }

    }

    private boolean isPsuedoProxy(Object object) {
        if(object instanceof org.jboss.weld.bean.proxy.ProxyObject) {
            BeanManager beanManager = CDI.current().getBeanManager();
            Class<?> clazz = object.getClass();
            do {
                Bean<?> spiBean = null;
                try{
                    spiBean = obtainResolvedBean(beanManager, clazz, object);
                } catch (Exception e) {
                    return false;
                }
                // Dependentスコープ or SingletonスコープのCDI Beanの場合 true。spiBean==nullまたはNormalScopeの場合false
                if (spiBean!=null) {
                    return !beanManager.isNormalScope(spiBean.getScope());
                }
                clazz = clazz.getSuperclass();
                // WELD-001318 AmbiguousResolutionExceptionを回避するため抽象度の高いクラス指定はここで処理する。
                if(clazz == Object.class) {
                    logger.warn("False is returned forcely to avoid an AmbiguousResolution. object: {} , class of object: {} , clazz:{} ." ,
                            object, object.getClass().getCanonicalName(), clazz.getCanonicalName() );
                    return false;
                }

            } while (clazz != Object.class);
            return false;
        } else {
            return object!=null;
        }
    }

    private Bean<?> obtainResolvedBean(BeanManager beanManager, Class<?> clazz, Object object) throws Exception {
        Bean<?> bean = null;
        try {
            bean = beanManager.resolve(beanManager.getBeans(clazz));
        } catch (Exception e){
            logger.warn(String.format("An exception occurred when executing clazz's beanManager resolve. object: %1$s , class of object: %2$s , clazz: %3$s , exception : %4$s , , exception message: %5$s ."
                            , object, object.getClass().getCanonicalName(), clazz.getCanonicalName(),
                            e.getClass().getCanonicalName(), e.getMessage() )
                    , e);
            throw e;
        }
        return bean;
    }

    private String generateMemoryUsage() {

        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        return "total:" + (total/(1024*1024)) + "MB, free:" + (free/(1024*1024)) + "MB, usage:" + ((total - free)/(1024*1024)) + "MB";
    }

}
