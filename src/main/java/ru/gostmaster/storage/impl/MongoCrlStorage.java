package ru.gostmaster.storage.impl;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.crl.Crl;
import ru.gostmaster.model.MongoCrlData;
import ru.gostmaster.storage.CRLStorage;

import java.util.List;
import java.util.function.Function;

/**
 * Реализация хранилища CRL в MongoDB.
 * 
 * @author maksimgurin 
 */
@Component
public class MongoCrlStorage implements CRLStorage {
    
    private ReactiveMongoTemplate reactiveMongoTemplate;
    
    @Override
    public Mono<Void> deleteAllCrls() {
        Query query = Query.query(new Criteria());
        return reactiveMongoTemplate.remove(query, MongoCrlData.class).then();
    }

    @Override
    public Mono<Crl> save(Crl crl) {
        // сохраняем. если уже есть скачанный с такого адреса - перетираем.
        Update update = Update.fromDocument(toDocument(crl));
        Query query = Query.query(Criteria.where(MongoCrlData.F_DONWLOADED_FROM).is(crl.getDownloadedFrom()));
        return reactiveMongoTemplate.upsert(query, update, MongoCrlData.class).thenReturn(crl);
    }
    
    @Override
    public Flux<Crl> getAllByIssuerKeys(List<String> issuerKeys) {
        Query query = Query.query(Criteria.where(MongoCrlData.F_ISSUER_KEY).in(issuerKeys));
        return reactiveMongoTemplate.find(query, MongoCrlData.class).map(Function.identity());
    }

    @Override
    public Flux<Crl> getAll() {
        return reactiveMongoTemplate.findAll(MongoCrlData.class).map(Function.identity());
    }

    @Autowired
    public void setReactiveMongoTemplate(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    private Document toDocument(Object object) {
        Document document = new Document();
        reactiveMongoTemplate.getConverter().write(object, document);
        return document;
    }
}
