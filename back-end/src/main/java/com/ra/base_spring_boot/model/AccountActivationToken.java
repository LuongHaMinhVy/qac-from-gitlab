package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_activation_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountActivationToken extends BaseObject {

    @Column(nullable = false, unique = true, length = 6)
    private String otp;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_activation_token_account"))
    private Account account;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}
