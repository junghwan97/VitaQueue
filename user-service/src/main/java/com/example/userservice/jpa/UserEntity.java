package com.example.userservice.jpa;


import com.example.userservice.dto.request.UserJoinRequest;
import com.example.userservice.jpa.UserRole;
import com.example.userservice.util.EncryptionUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() where id=?")
@SQLRestriction("deleted_at is NULL")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "registered_at")
    private Timestamp registeredAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @PrePersist
    void registeredAT() {
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static UserEntity of(UserJoinRequest requestDto, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(requestDto.getEmail());
        userEntity.setPassword(password);
        userEntity.setName(requestDto.getName());
        userEntity.setAddress(requestDto.getAddress());
        userEntity.setPhone(requestDto.getPhone());
        userEntity.setRole(UserRole.USER);
        return userEntity;
    }
    public static UserEntity decryptSensitiveData(UserEntity user) throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(EncryptionUtil.decrypt(user.email));
        userEntity.setName(EncryptionUtil.decrypt(user.name));
        userEntity.setAddress(EncryptionUtil.decrypt(user.address));
        userEntity.setPhone(EncryptionUtil.decrypt(user.phone));
        return userEntity;
    }
}
