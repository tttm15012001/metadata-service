package com.metadataservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * A reusable reactive HTTP client for external APIs (TMDb, OMDb, etc.)
 * Handles:
 *  - URI building with dynamic query parameters
 *  - Consistent error handling
 *  - Centralized logging
 *  - Reactive response deserialization
 */
@Slf4j
@Component
public class ReactiveApiClient {

    /**
     * Generic GET request with query parameters and response mapping.
     *
     * @param client  a configured WebClient instance
     * @param path    endpoint path (e.g. "/search/tv")
     * @param params  query parameters map (may be null)
     * @param clazz   expected response class
     * @param source  tag for logging (e.g. "TMDb", "OMDb")
     * @return a reactive Mono of type T
     */
    public <T> Mono<T> get(
            WebClient client,
            String path,
            Map<String, Object> params,
            Class<T> clazz,
            String source
    ) {
        return client.get()
            .uri(uriBuilder -> {
                uriBuilder.path(path);
                if (params != null) {
                    params.forEach(uriBuilder::queryParam);
                }
                return uriBuilder.build();
            })
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("[{}] - returned error {}:\n{}", source, response.statusCode(), body);
                        return Mono.error(new RuntimeException("[" + source + "]" + " - API error " + response.statusCode()));
                    })
            )
            .bodyToMono(clazz)
            .doOnNext(r -> {
                try {
                    log.info("[{}] - Call Successfully", source);
                } catch (Exception e) {
                    log.error("[{}] - Failed to log response", source, e);
                }
            });
    }

    /**
     * Overload for simple GETs without query params.
     */
    public <T> Mono<T> get(WebClient client, String path, Class<T> clazz, String source) {
        return get(client, path, null, clazz, source);
    }
}
