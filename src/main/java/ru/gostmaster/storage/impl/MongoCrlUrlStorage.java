package ru.gostmaster.storage.impl;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.crl.Crl;
import ru.gostmaster.data.crl.CrlUrl;
import ru.gostmaster.model.MongoCrlUrlData;
import ru.gostmaster.storage.CRLUrlStorage;

import java.util.function.Function;

/**
 * Реализация хранилища ссылок для MongoDB.
 *
 * @author maksimgurin
 */
@Component
public class MongoCrlUrlStorage implements CRLUrlStorage {

    @Setter(onMethod_ = {@Autowired})
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Void> add(String url) {
        MongoCrlUrlData crlUrlData = new MongoCrlUrlData();
        crlUrlData.setUrl(url);
        // если есть сущность с такой ссылкой - не вставляем
        Query query = Query.query(Criteria.where(MongoCrlUrlData.F_URL).is(url));
        Update update = new Update().setOnInsert(MongoCrlUrlData.F_URL, url);
        return reactiveMongoTemplate.upsert(query, update, MongoCrlUrlData.class).then();
    }

    @Override
    public Flux<CrlUrl> getByUrl(String url) {
        Query query = Query.query(Criteria.where(MongoCrlUrlData.F_URL).is(url));
        return reactiveMongoTemplate.find(query, MongoCrlUrlData.class).map(Function.identity());
    }

    @Override
    public Flux<CrlUrl> getAll() {
        return reactiveMongoTemplate.findAll(MongoCrlUrlData.class).map(Function.identity());
    }

    @Override
    public Mono<Void> update(Crl crl) {
        Query query = Query.query(Criteria.where(MongoCrlUrlData.F_URL).is(crl.getDownloadedFrom()));
        Update update = new Update().set(MongoCrlUrlData.F_CURRENT_UPDATE, crl.getThisUpdate())
            .set(MongoCrlUrlData.F_NEXT_UPDATE, crl.getNextUpdate());
        return reactiveMongoTemplate.updateFirst(query, update, MongoCrlUrlData.class).then();
    }
}
