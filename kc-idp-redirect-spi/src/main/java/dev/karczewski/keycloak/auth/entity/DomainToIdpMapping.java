package dev.karczewski.keycloak.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "domain_to_idp")
public class DomainToIdpMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_domain", nullable = false, unique = true)
    private String emailDomain;

    @Column(name = "idp_alias", nullable = false)
    private String idpAlias;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    // Constructors
    public DomainToIdpMapping() {}

    public DomainToIdpMapping(final String emailDomain, final String idpAlias) {
        this.emailDomain = emailDomain;
        this.idpAlias = idpAlias;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    public String getIdpAlias() {
        return idpAlias;
    }

    public void setIdpAlias(String idpAlias) {
        this.idpAlias = idpAlias;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
