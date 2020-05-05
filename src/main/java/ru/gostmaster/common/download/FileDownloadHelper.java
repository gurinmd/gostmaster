package ru.gostmaster.common.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class FileDownloadHelper {
    
    private static final String DEFAUL_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36";
    
    private RestTemplate restTemplate;

    public Mono<Pair<String,byte[]>> download(String url) {
        return Mono.create(monoSink -> {
            String fetchUrl = url;
            int status = -1;
            try {
                ResponseEntity<byte[]> exchange;
                do {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.USER_AGENT,DEFAUL_USER_AGENT);
                    HttpEntity requestEntity = new HttpEntity(httpHeaders);
                    
                    exchange = restTemplate.exchange(fetchUrl, HttpMethod.GET, requestEntity, byte[].class);
                    status = exchange.getStatusCodeValue();
                    if (HttpStatus.resolve(status).is3xxRedirection()) {
                        fetchUrl = exchange.getHeaders().get(HttpHeaders.LOCATION).get(0);
                    }
                } while (HttpStatus.resolve(status).is3xxRedirection());
                Pair<String, byte[]> resPair = Pair.of(url,exchange.getBody());
                monoSink.success(resPair);
            } catch (Exception ex) {
                log.error("Error downloading from {}. Cause {}", url, ex.getMessage());
                // нельзя тут ошибку. иначе пайп порвем
                monoSink.success(Pair.of(url,new byte[0]));
            }
        });
    }
    
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
