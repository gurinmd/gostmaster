package ru.gostmaster.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.spi.storage.CRLStorage;
import ru.gostmaster.model.MongoCrlData;

import java.util.List;
import java.util.function.Function;

@Component
public class MongoCrlStorage implements CRLStorage {
    
    private ReactiveMongoTemplate reactiveMongoTemplate;
    

    @Override
    public Mono<Void> deleteAllCrls() {
        Query query = Query.query(new Criteria());
        return reactiveMongoTemplate.remove(query, MongoCrlData.class).then();
    }

    @Override
    public Mono<Void> saveAllCrls(Flux<Crl> crlFlux) {
        return crlFlux.flatMap(crl -> reactiveMongoTemplate.save(crl)).then();
    }
    
    @Override
    public Flux<Crl> getAllByDownloadedFromOrCa(List<String> downloadedFrom, List<String> ca) {
        Query query = Query.query(Criteria.where(MongoCrlData.F_DONWLOADED_FROM).in(downloadedFrom)
            .orOperator(Criteria.where(MongoCrlData.F_CA_SUBJECT).in(ca)));
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
}
