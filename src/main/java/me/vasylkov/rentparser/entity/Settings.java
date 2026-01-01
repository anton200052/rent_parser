package me.vasylkov.rentparser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // всегда 1

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "work_from", nullable = false)
    private String workFrom;

    @Column(name = "work_until", nullable = false)
    private String workUntil;

    @Column(name = "interval_minutes", nullable = false)
    private int intervalMinutes;
}