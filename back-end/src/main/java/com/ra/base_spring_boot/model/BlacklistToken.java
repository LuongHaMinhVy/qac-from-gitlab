package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 800)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}
