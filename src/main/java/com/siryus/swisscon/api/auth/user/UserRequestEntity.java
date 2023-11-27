package com.siryus.swisscon.api.auth.user;

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
@Table(name = "user_request")
public class UserRequestEntity {
    public static final int DETAILS_COLUMN_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_ip")
    private String userIp;
    
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    
    @Column(name = "type")
    @Enumerated(EnumType.STRING)    
    private UserRequestType type;    

    @Column(name = "details", length = DETAILS_COLUMN_LENGTH)
    private String details;
}
