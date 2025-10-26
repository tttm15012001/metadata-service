package com.metadataservice.model.entity;

import com.metadataservice.model.Gender;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "actor_id", unique = true, nullable = false)
    protected Integer actorId;

    @Column(name = "name")
    protected String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    protected Gender gender;

    @Column(name = "character_name")
    protected String character_name;

    @Column(name = "profile_path")
    protected String profilePath;
}
