package dev.karczewski.keycloak.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.validation.Validation;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class IdpRedirectAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(IdpRedirectAuthenticator.class);

    private static final String EMAIL_NOTE = "mapped_email";
    private static final String IDP_HINT_PARAM = "kc_idp_hint";
    private static final String LOGIN_HINT_PARAM = "login_hint";
    private static final String EMAIL_FORM_TEMPLATE = "email-form.ftl";
    private static final String USERNAME_FIELD = "username";

    private static final String ERROR_INVALID_EMAIL = "Invalid email";
    private static final String ERROR_UNSUPPORTED_DOMAIN = "Unsupported email domain";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (hasExistingIdpHint(context)) {
            logger.debug("URL already contains kc_idp_hint, skipping email form");
            context.success();
            return;
        }

        presentEmailForm(context);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String email = extractEmailFromForm(context);

        if (!isValidEmail(email)) {
            presentEmailFormWithError(context, ERROR_INVALID_EMAIL);
            return;
        }

        String domain = extractDomainFromEmail(email);
        Optional<String> idpAlias = resolveIdpAlias(context.getSession(), domain);

        if (idpAlias.isEmpty()) {
            logger.warnf("No IdP mapping found for domain: %s", domain);
            presentEmailFormWithError(context, ERROR_UNSUPPORTED_DOMAIN);
            return;
        }

        processSuccessfulMapping(context, email, idpAlias.get());
    }

    private boolean hasExistingIdpHint(AuthenticationFlowContext context) {
        return context.getHttpRequest()
                .getUri()
                .getQueryParameters()
                .getFirst(IDP_HINT_PARAM) != null;
    }

    private void presentEmailForm(AuthenticationFlowContext context) {
        Response form = context.form()
                .setAttribute(USERNAME_FIELD, "")
                .createForm(EMAIL_FORM_TEMPLATE);
        context.challenge(form);
    }

    private void presentEmailFormWithError(AuthenticationFlowContext context, String errorMessage) {
        Response challenge = context.form()
                .setError(errorMessage)
                .createForm(EMAIL_FORM_TEMPLATE);
        context.challenge(challenge);
    }

    private String extractEmailFromForm(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        return formData.getFirst(USERNAME_FIELD);
    }

    private boolean isValidEmail(String email) {
        return Validation.isEmailValid(email);
    }

    private String extractDomainFromEmail(String email) {
        return email.substring(email.indexOf("@") + 1).toLowerCase(Locale.ROOT);
    }

    private Optional<String> resolveIdpAlias(KeycloakSession session, String domain) {
        try (EntityManagerWrapper emWrapper = new EntityManagerWrapper(session)) {
            TypedQuery<String> query = emWrapper.getEntityManager()
                    .createQuery("SELECT d.idpAlias FROM DomainToIdpMapping d WHERE d.emailDomain = :domain AND d.enabled = true", String.class)
                    .setParameter("domain", domain);

            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            logger.debugf("No IdP mapping found for domain: %s", domain);
            return Optional.empty();
        } catch (Exception e) {
            logger.errorf(e, "Error resolving IdP alias for domain: %s", domain);
            return Optional.empty();
        }
    }

    private void processSuccessfulMapping(AuthenticationFlowContext context, String email, String idpAlias) {
        context.getAuthenticationSession().setClientNote(IDP_HINT_PARAM, idpAlias);
        context.getAuthenticationSession().setAuthNote(EMAIL_NOTE, email);

        URI redirectUri = buildRedirectUri(context, email, idpAlias);
        context.forceChallenge(Response.seeOther(redirectUri).build());
    }

    private URI buildRedirectUri(AuthenticationFlowContext context, String email, String idpAlias) {
        Map<String, String> clientNotes = context.getAuthenticationSession().getClientNotes();
        String state = clientNotes.get(OAuth2Constants.STATE);
        String nonce = clientNotes.get("nonce");

        // build redirect with kc_idp_hint param
        UriBuilder uriBuilder = UriBuilder.fromUri(context.getUriInfo().getBaseUri())
                .path("realms")
                .path(context.getRealm().getName())
                .path("protocol")
                .path("openid-connect")
                .path("auth")
                .queryParam(OAuth2Constants.CLIENT_ID, context.getAuthenticationSession().getClient().getClientId())
                .queryParam(OAuth2Constants.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
                .queryParam(OAuth2Constants.RESPONSE_TYPE, "code")
                .queryParam(OAuth2Constants.SCOPE, "openid")
                .queryParam(IDP_HINT_PARAM, idpAlias)
                .queryParam(LOGIN_HINT_PARAM, email);

        // Only add state and nonce if they exist
        if (state != null) {
            uriBuilder.queryParam(OAuth2Constants.STATE, state);
        }
        if (nonce != null) {
            uriBuilder.queryParam("nonce", nonce);
        }

        return uriBuilder.build();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No required actions needed
    }

    @Override
    public void close() {
        // Resource cleanup handled by EntityManagerWrapper
    }

    /**
     * Helper class for proper EntityManager resource management
     */
    private static class EntityManagerWrapper implements AutoCloseable {
        private final EntityManager entityManager;

        public EntityManagerWrapper(KeycloakSession session) {
            JpaConnectionProvider jpa = session.getProvider(JpaConnectionProvider.class);
            this.entityManager = jpa.getEntityManager();
        }

        public EntityManager getEntityManager() {
            return entityManager;
        }

        @Override
        public void close() {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}