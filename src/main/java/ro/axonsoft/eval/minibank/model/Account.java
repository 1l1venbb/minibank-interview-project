package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false, scale = 2, precision = 19)
    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Account(String ownerName, String iban, String currency, AccountType type, BigDecimal balance) {
        this.ownerName = ownerName;
        this.iban = iban;
        this.currency = currency;
        this.type = type;
        this.balance = balance;
    }

    public Account() {
    }


    @PrePersist
    void onCreate() {
        if (this.balance == null)
            this.balance = BigDecimal.ZERO;
        if (this.createdAt == null)
            this.createdAt = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
