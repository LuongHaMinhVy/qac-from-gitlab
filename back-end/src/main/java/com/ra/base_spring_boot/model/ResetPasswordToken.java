package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reset_password_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordToken extends BaseObject {

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reset_token_account"))
    private Account account;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}

