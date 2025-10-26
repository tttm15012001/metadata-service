package com.metadataservice.model.entity;

import com.metadataservice.dto.response.MetadataResponseDto;
import com.metadataservice.utils.CommonUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.metadataservice.common.constant.DatabaseConstants.TABLE_METADATA;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TABLE_METADATA)
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true, nullable = false, name = "movie_id")
    protected Long movieId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "metadata_actor_mapping",
        joinColumns = @JoinColumn(name = "metadata_id"),
        inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private List<Actor> actors;

    @Column(name = "search_title")
    protected String searchTitle;

    @Column(name = "tmdb_id")
    protected Integer tmdbId;

    @Column(name = "for_adult")
    protected Boolean forAdult;

    @Column(name = "title")
    protected String title;

    @Column(name = "original_title")
    protected String originalTitle;

    @Column(name = "description", length = 2000)
    protected String description;

    @Column(name = "number_of_episodes")
    protected Integer numberOfEpisodes;

    @Column(name = "vote_average")
    protected Double voteAverage;

    @Column(name = "vote_count")
    protected Integer voteCount;

    @Column(name = "popularity")
    protected Double popularity;

    @Column(name = "poster_path")
    protected String posterPath;

    @Column(name = "backdrop_path")
    protected String backdropPath;

    @Column(name = "release_date")
    protected LocalDate releaseDate;

    @Column(name = "country")
    protected String country;

    @Column(name = "original_language")
    protected String originalLanguage;

    @Column(name = "genre")
    protected String genre;

    @Column(name = "status")
    protected String status;

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    protected LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    @LastModifiedDate
    protected LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    public MetadataResponseDto toMetadataResponseDto() {
        return MetadataResponseDto.builder()
                .id(this.getId())
                .tmdbId(this.getTmdbId())
                .forAdult(this.getForAdult())
                .title(this.getTitle())
                .originalTitle(this.getOriginalTitle())
                .description(this.getDescription())
                .numberOfEpisodes(this.getNumberOfEpisodes())
                .voteAverage(this.getVoteAverage())
                .voteCount(this.getVoteCount())
                .popularity(this.getPopularity())
                .posterPath(this.getPosterPath())
                .backdropPath(this.getBackdropPath())
                .releaseDate(this.getReleaseDate())
                .country(this.getCountry())
                .originalLanguage(this.getOriginalLanguage())
                .genre(CommonUtil.convertStringToList(this.genre))
                .status(this.getStatus())
                .build();
    }
}
