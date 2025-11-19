package com.metadataservice.service.impl;

import com.metadataservice.dto.kafka.CrawlMovieResultMessage;
import com.metadataservice.dto.response.MetadataResponseDto;
import com.metadataservice.exception.NotFoundException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.metadataservice.common.constant.DatabaseConstants.TABLE_METADATA;

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
                .title(metadata.getTitle())
                .backdrop(metadata.getBackdropPath())
                .metadataId(metadata.getId())
                .genres(metadata.getGenre())
                .voteAverage(metadata.getVoteAverage())
                .build();
        return crawlMovieResultProducer.sendCrawlResult(movieId, message, responseTopic);
    }

    @Override
    public Metadata saveMetadata(Metadata metadata) {
        return this.metadataRepository.save(metadata);
    }

    @Override
    public MetadataResponseDto getMetadataById(Long metadataId) {
        Metadata metadata = this.metadataRepository.findByIdWithActors(metadataId)
                .orElseThrow(() -> new NotFoundException(TABLE_METADATA, metadataId));
        return metadata.toMetadataResponseDto();
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

    @Transactional
    protected Mono<Metadata> saveRetrievedData(Metadata metadata) {
        String searchTitle = metadata.getSearchTitle();
        Long movieId = metadata.getMovieId();

        return Mono.fromCallable(() -> {
            if (metadata.getActors() == null || metadata.getActors().isEmpty()) {
                return getMetadataByMovieIdOrSearchTitle(movieId, searchTitle)
                        .map(existing -> mergeAndSave(existing, metadata))
                        .orElseGet(() -> metadataRepository.save(metadata));
            }

            List<Integer> actorIds = metadata.getActors().stream()
                    .map(Actor::getActorId)
                    .collect(Collectors.toList());

            Map<Integer, Actor> existingActors = actorRepository.findByActorIdIn(actorIds)
                    .stream()
                    .collect(Collectors.toMap(Actor::getActorId, a -> a));

            List<Actor> persistentActors = metadata.getActors().stream()
                    .map(actor -> {
                        Actor existing = existingActors.get(actor.getActorId());
                        if (existing != null) {
                            return existing;
                        }

                        try {
                            return actorRepository.save(actor);
                        } catch (DataIntegrityViolationException e) {
                            log.warn("Another thread may have just inserted this actor");
                            return actorRepository.findByActorId(actor.getActorId())
                                    .orElseThrow(() -> e);
                        }
                    })
                    .collect(Collectors.toList());

            metadata.setActors(persistentActors);

            return this.getMetadataByMovieIdOrSearchTitle(movieId, searchTitle)
                    .map(existing -> mergeAndSave(existing, metadata))
                    .orElseGet(() -> metadataRepository.save(metadata));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Metadata mergeAndSave(Metadata existing, Metadata incoming) {
        existing.setVoteAverage(incoming.getVoteAverage());
        existing.setVoteCount(incoming.getVoteCount());
        existing.setPosterPath(incoming.getPosterPath());
        existing.setBackdropPath(incoming.getBackdropPath());
        existing.setActors(incoming.getActors());
        return metadataRepository.save(existing);
    }
}