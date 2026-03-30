package com.solarlab.adboard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreated() {
        uploadedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        Image image = (Image) o;
        return id != null && id.equals(image.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Image{"
                + "id=" + id
                + ", advertisement_id=" + advertisement.getId()
                + ", url='" + url + '\''
                + ", sort_order=" + sortOrder
                + ", uploaded_at=" + uploadedAt
                + '}';
    }
}
