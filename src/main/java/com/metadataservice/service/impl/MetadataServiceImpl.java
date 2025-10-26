package com.metadataservice.service.impl;

import com.metadataservice.dto.kafka.CrawlMovieResultMessage;
import com.metadataservice.messaging.producer.CrawlMovieResultProducer;
import com.metadataservice.model.entity.Actor;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.repository.ActorRepository;
import com.metadataservice.repository.MetadataRepository;
import com.metadataservice.service.MetadataProvider;
import com.metadataservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    private final List<MetadataProvider> providers;

    private final MetadataRepository metadataRepository;

    private final CrawlMovieResultProducer crawlMovieResultProducer;

    private final ActorRepository actorRepository;

    @Autowired
    public MetadataServiceImpl(
            List<MetadataProvider> providers,
            MetadataRepository metadataRepository,
            CrawlMovieResultProducer crawlMovieResultProducer,
            ActorRepository actorRepository
    ) {
        this.metadataRepository = metadataRepository;
        this.crawlMovieResultProducer = crawlMovieResultProducer;
        this.providers = providers;
        this.actorRepository = actorRepository;
    }

    @Override
    public Mono<Void> crawl(Long movieId, String title, Integer year, String responseTopic) {
        return Mono.zip(
            providers.stream()
                .map(p -> p.fetch(movieId, title, year))
                .collect(Collectors.toList()),
            this::mergeAllMetadata
        ).flatMap(this::saveRetrievedData)
        .flatMap(saved -> this.returnRetrievedData(movieId, saved, responseTopic))
        .then();
    }

    @Override
    public Optional<Metadata> getMetadataByMovieIdOrSearchTitle(Long movieId, String title) {
        return metadataRepository.findByMovieIdOrSearchTitle(movieId, title);
    }

    @Override
    public Mono<Void> returnRetrievedData(Long movieId, Metadata metadata, String responseTopic) {
        var message = CrawlMovieResultMessage.builder()
                .movieId(movieId)
                .metadataId(metadata.getId())
                .numberOfEpisodes(metadata.getNumberOfEpisodes())
                .voteAverage(metadata.getVoteAverage())
                .build();
        return crawlMovieResultProducer.sendCrawlResult(movieId, message, responseTopic);
    }

    @Override
    public Metadata saveMetadata(Metadata metadata) {
        return this.metadataRepository.save(metadata);
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
        Long movieId = metadata.getMovieId();

        return Mono.fromCallable(() -> {
            List<Actor> persistentActors = metadata.getActors().stream()
                    .map(actor -> actorRepository.findByActorId(actor.getActorId())
                            .orElseGet(() -> actorRepository.save(actor))
                    )
                    .collect(Collectors.toList());

            metadata.setActors(persistentActors);

            return this.getMetadataByMovieIdOrSearchTitle(movieId, searchTitle)
                    .map(existing -> {
                        existing.setVoteAverage(metadata.getVoteAverage());
                        existing.setVoteCount(metadata.getVoteCount());
                        existing.setPosterPath(metadata.getPosterPath());
                        existing.setActors(persistentActors); // update relations
                        return metadataRepository.save(existing);
                    })
                    .orElseGet(() -> metadataRepository.save(metadata));
        }).subscribeOn(Schedulers.boundedElastic());
    }
}