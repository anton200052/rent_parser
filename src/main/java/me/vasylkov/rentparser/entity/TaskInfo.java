package me.vasylkov.rentparser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "task_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "provider_type", nullable = false, length = 50)),
            @AttributeOverride(name = "providerUrl", column = @Column(name = "provider_url", nullable = false, length = 255))
    })
    private Provider provider;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "token", column = @Column(name = "telegram_token", nullable = false, length = 200))
    })
    private TelegramNotificationProvider telegramNotification;

    @Column(name = "iterations", nullable = false)
    private long iterations;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "task_handled_listings", // Имя нашей новой таблицы-связки
            joinColumns = @JoinColumn(name = "task_info_id"), // Колонка, ссылающаяся на TaskInfo
            inverseJoinColumns = @JoinColumn(name = "listing_id") // Колонка, ссылающаяся на Listing
    )
    private Set<Listing> handledListings = new HashSet<>();

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Provider {
        @Enumerated(EnumType.STRING)
        private ProviderType type;
        private String providerUrl;
    }

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TelegramNotificationProvider {

        @Column(nullable = false, length = 200)
        private String token;

        @ElementCollection
        @CollectionTable(
                name = "telegram_chat_ids",
                joinColumns = @JoinColumn(name = "task_info_id")
        )
        @Column(name = "chat_id", nullable = false, length = 100)
        private List<String> chatIds = new ArrayList<>();
    }

    public enum ProviderType {
        IMMO_SCOUT
    }
}
