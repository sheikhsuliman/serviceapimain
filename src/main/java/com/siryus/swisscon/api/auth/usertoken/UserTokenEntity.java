package com.siryus.swisscon.api.auth.usertoken;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_token")
public class UserTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "external_id")
    private String externalId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, updatable = false)
    private UserTokenType type;    
    
    @Column(name = "expires_on")
    private LocalDateTime expiresOn;
}
