package com.example.securedoc.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

import static com.example.securedoc.constant.Constants.*;
import static java.time.LocalDateTime.now;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Entity
@Table(name = "confirmations")
public class ConfirmationEntity extends Auditable {
    private String key;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("user_id")
    private UserEntity user;

    public ConfirmationEntity(UserEntity user) {
        this.user = user;
        this.key = UUID.randomUUID().toString();
    }

    public boolean isExpired() {
        return getCreatedAt().plusMinutes(MAXIMUM_CONFIRMATION_DURATION).isBefore(now());
    }
}
