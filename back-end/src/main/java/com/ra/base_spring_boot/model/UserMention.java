package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.MentionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "UserMentions")
public class UserMention extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "CommentID", nullable = false)
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "MentionedAccountID", nullable = false)
    private Account mentionedAccount;

    @ManyToOne
    @JoinColumn(name = "MentionerAccountID", nullable = false)
    private Account mentionerAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private MentionStatus status = MentionStatus.pending;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
