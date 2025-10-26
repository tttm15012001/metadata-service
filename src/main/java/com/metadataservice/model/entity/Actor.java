package com.metadataservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Meta;

import java.util.List;

import static com.metadataservice.common.constant.DatabaseConstants.TABLE_ACTOR;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TABLE_ACTOR)
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToMany(mappedBy = "actors", fetch = FetchType.LAZY)
    private List<Metadata> metadata;

    @Column(name = "actor_id", nullable = false)
    protected Integer actorId;

    @Column(name = "character")
    protected String character;

    @Column(name = "profile_path")
    protected String profilePath;
}
