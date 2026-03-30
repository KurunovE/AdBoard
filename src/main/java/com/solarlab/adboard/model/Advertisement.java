package com.solarlab.adboard.model;

import com.solarlab.adboard.enums.AdvertisementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "advertisements")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "advertisement_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdvertisementStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(
            mappedBy = "advertisement",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(
            mappedBy = "advertisement",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Image> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Advertisement)) return false;
        Advertisement advertisement = (Advertisement) o;
        return id != null && id.equals(advertisement.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Advertisement{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", price=" + price
                + ", status=" + status
                + ", author_id=" + author.getId()
                + ", category_id=" + category.getId()
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }
}
