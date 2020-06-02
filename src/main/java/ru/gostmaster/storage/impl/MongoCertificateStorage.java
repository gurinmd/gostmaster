package ru.gostmaster.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GraphLookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.model.MongoCertificateData;
import ru.gostmaster.storage.CertificateStorage;
import ru.gostmaster.util.CollectionsUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Реализация хранилища сертификатов в MongoDB.
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class MongoCertificateStorage implements CertificateStorage {
    
    private ReactiveMongoTemplate reactiveMongoTemplate;
    
    @Override
    public Mono<List<Certificate>> getCertificateChainForLeafKey(String subjectKey) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(MongoCertificateData.F_SUBJECT_KEY).is(subjectKey));
        GraphLookupOperation graphLookupOperation = Aggregation
            .graphLookup(MongoCertificateData.COLLECTION)
            .startWith(MongoCertificateData.F_SUBJECT_EXPRESSION)
            .connectFrom(MongoCertificateData.F_ISSUER_KEY)
            .connectTo(MongoCertificateData.F_SUBJECT_KEY)
            .as(MongoCertificateData.F_CHAIN);
        Aggregation aggregation = Aggregation.newAggregation(matchOperation, graphLookupOperation);
        
        Flux<MongoCertificateData> documentFlux = reactiveMongoTemplate.aggregate(aggregation, 
            MongoCertificateData.class, MongoCertificateData.class);
        
        Flux<Certificate> certificateFlux = documentFlux.flatMap(cert -> Flux.fromIterable(cert.getChain()));
        return certificateFlux.collectList()
            .map(certificates -> CollectionsUtils.makeUniqueList(certificates, Certificate::getSn));
    }

    @Override
    public Mono<Void> saveAllCertificates(Flux<Certificate> certificateFlux) {
        Flux<Certificate> savedCertFlux = certificateFlux
            .parallel()
            .runOn(Schedulers.newElastic("cert-save-thread-pool"))
            .flatMap(certificate -> {
                if (certificate.isTrusted()) {
                    return saveTrustedCertificate(certificate);
                } else {
                    return saveIntermediateCertificate(certificate);
                }
            }).sequential();
        return savedCertFlux.then();
    }

    @Override
    public Mono<Void> deleteAllTrusted() {
        Query query = Query.query(Criteria.where(MongoCertificateData.F_TRUSTED).is(true));
        return reactiveMongoTemplate.remove(query, MongoCertificateData.class).then();
    }

    @Override
    public Mono<Void> deleteAllIntermediate() {
        Query query = Query.query(Criteria.where(MongoCertificateData.F_TRUSTED).is(false));
        return reactiveMongoTemplate.remove(query, MongoCertificateData.class).then();
    }

    @Override
    public Flux<Certificate> getAll() {
        return reactiveMongoTemplate.findAll(MongoCertificateData.class).map(Function.identity());
    }

    @Autowired
    public void setReactiveMongoTemplate(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }
    
    private Mono<Certificate> saveTrustedCertificate(Certificate mongoCertificateData) {
        // если находит серт с таким серийником - перезаписываем. Потому что новый - доверенный, 
        // и он должен сохранить как доверенный.
        Query query = Query.query(Criteria.where(MongoCertificateData.F_SN).is(mongoCertificateData.getSn()));
        Update update = Update.fromDocument(toDocument(mongoCertificateData), MongoCertificateData.F_TRUSTED)
            .setOnInsert(MongoCertificateData.F_TRUSTED, true);
        return reactiveMongoTemplate.upsert(query, update, MongoCertificateData.class)
            .thenReturn(mongoCertificateData);
    }
    
    private Mono<Certificate> saveIntermediateCertificate(Certificate mongoCertificateData) {
        // если находим сертификат с таким серийником - перезаписываем все, кроме поля TRUSTED
        Query query = Query.query(Criteria.where(MongoCertificateData.F_SN).is(mongoCertificateData.getSn()));
        Update update = Update.fromDocument(toDocument(mongoCertificateData), MongoCertificateData.F_TRUSTED)
            .setOnInsert(MongoCertificateData.F_TRUSTED, mongoCertificateData.isTrusted());
        return reactiveMongoTemplate.upsert(query, update, MongoCertificateData.class)
            .thenReturn(mongoCertificateData);
    }
    
    private Document toDocument(Object object) {
        Document document = new Document();
        reactiveMongoTemplate.getConverter().write(object, document);
        return document;
    }
}
