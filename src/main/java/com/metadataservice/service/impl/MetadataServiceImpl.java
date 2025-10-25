package com.metadataservice.service.impl;

import com.metadataservice.dto.kafka.CrawlMovieResultMessage;
import com.metadataservice.messaging.producer.CrawlMovieResultProducer;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.repository.MetadataRepository;
import com.metadataservice.service.MetadataProvider;
import com.metadataservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    @Value("${config.tmdb.base-url}")
    private String tmdbBaseUrl;

    @Value("${config.tmdb.token}")
    private String tokenTmDb;

    @Value("${config.omdb.base-url}")
    private String omdbBaseUrl;

    @Value("${config.omdb.token}")
    private String tokenOmDb;

    @Value("${config.tmdb.language}")
    private String language;

    private final List<MetadataProvider> providers;

    private final MetadataRepository metadataRepository;

    private final CrawlMovieResultProducer crawlMovieResultProducer;

    @Autowired
    public MetadataServiceImpl(
        List<MetadataProvider> providers,
        MetadataRepository metadataRepository,
        CrawlMovieResultProducer crawlMovieResultProducer
    ) {
        this.metadataRepository = metadataRepository;
        this.crawlMovieResultProducer = crawlMovieResultProducer;
        this.providers = providers;
    }

    @Override
    public Mono<Void> crawl(Long movieId, String title, Integer year) {
        return Mono.zip(
            providers.stream()
                .map(p -> p.fetch(movieId, title, year))
                .collect(Collectors.toList()),
            this::mergeAllMetadata
        ).flatMap(this::saveRetrievedData)
        .flatMap(saved -> {
            var message = CrawlMovieResultMessage.builder()
                .movieId(movieId)
                .metadataId(saved.getId())
                .numberOfEpisodes(saved.getNumberOfEpisodes())
                .voteAverage(saved.getVoteAverage())
                .build();
            return crawlMovieResultProducer.sendCrawlResult(message);
        })
        .then();
    }

    private Metadata mergeAllMetadata(Object[] results) {
        Metadata merged = new Metadata();
        for (Object r : results) {
            Metadata partial = (Metadata) r;
            BeanUtils.copyProperties(partial, merged, getNullProperties(partial));
        }
        return merged;
    }

    private String[] getNullProperties(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors())
            .map(PropertyDescriptor::getName)
            .filter(name -> src.getPropertyValue(name) == null)
            .toArray(String[]::new);
    }

    private Mono<Metadata> saveRetrievedData(Metadata metadata) {
        String searchTitle = metadata.getSearchTitle();
        return Mono.fromCallable(() ->
            metadataRepository.findBySearchTitle(searchTitle)
                .map(existing -> {
                    existing.setVoteAverage(metadata.getVoteAverage());
                    existing.setVoteCount(metadata.getVoteCount());
                    existing.setPosterPath(metadata.getPosterPath());
                    return metadataRepository.save(existing);
                })
                .orElseGet(() -> metadataRepository.save(metadata))
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
