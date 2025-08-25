CREATE TABLE IF NOT EXISTS domain_to_idp (
  email_domain TEXT PRIMARY KEY,
  idp_alias TEXT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO domain_to_idp (email_domain, idp_alias, enabled)
VALUES ('partner.com', 'external-idp', TRUE)
ON CONFLICT (email_domain) DO NOTHING;
