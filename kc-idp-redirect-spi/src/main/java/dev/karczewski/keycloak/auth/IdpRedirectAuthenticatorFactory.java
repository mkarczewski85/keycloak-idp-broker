package dev.karczewski.keycloak.auth;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class IdpRedirectAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "domain-to-idp-redirect";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new IdpRedirectAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
        // pass
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // pass
    }

    @Override
    public void close() {
        // pass
    }

    @Override
    public String getDisplayType() {
        return "Domain to IdP Redirect Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "Identity Provider";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Maps email domain to a configured external Identity Provider alias and redirect with kc_idp_hint";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

}
