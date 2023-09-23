package dev.avatsav.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.collect.Lists;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.social.google.GoogleIdentityProvider;
import org.keycloak.social.google.GoogleIdentityProviderConfig;

public class GoogleOneTapIdentityProvider extends GoogleIdentityProvider {
  protected static final Logger logger = Logger.getLogger(GoogleOneTapIdentityProvider.class);
  private static final List<String> validSubjectTokenTypes =
      Lists.newArrayList(OAuth2Constants.ID_TOKEN_TYPE, OAuth2Constants.ACCESS_TOKEN_TYPE);

  public GoogleOneTapIdentityProvider(
      KeycloakSession session, GoogleIdentityProviderConfig config) {
    super(session, config);
  }

  private final GoogleIdTokenVerifier googleIdTokenVerifier =
      new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
          .setAudience(Collections.singletonList(getConfig().getClientId()))
          .build();

  @Override
  protected BrokeredIdentityContext exchangeExternalUserInfoValidationOnly(
      EventBuilder event, MultivaluedMap<String, String> params) {
    String subjectToken = params.getFirst(OAuth2Constants.SUBJECT_TOKEN);
    if (subjectToken == null) {
      event.detail(Details.REASON, OAuth2Constants.SUBJECT_TOKEN + " param unset");
      event.error(Errors.INVALID_TOKEN);
      throw new ErrorResponseException(
          OAuthErrorException.INVALID_TOKEN, "token not set", Response.Status.BAD_REQUEST);
    }

    String subjectTokenType = params.getFirst(OAuth2Constants.SUBJECT_TOKEN_TYPE);
    if (subjectTokenType == null) {
      logger.warn("subject_token_type is null. Defaulting to access_token type");
      subjectTokenType = OAuth2Constants.ACCESS_TOKEN_TYPE;
    }
    if (!validSubjectTokenTypes.contains(subjectTokenType)) {
      event.detail(
          Details.REASON, OAuth2Constants.SUBJECT_TOKEN_TYPE + " invalid:" + subjectTokenType);
      event.error(Errors.INVALID_TOKEN_TYPE);
      logger.error("Invalid subjectTokenType: " + subjectTokenType);
      throw new ErrorResponseException(
          OAuthErrorException.INVALID_TOKEN, "invalid token type", Response.Status.BAD_REQUEST);
    }
    return validateExternalTokenThroughUserInfo(event, subjectToken, subjectTokenType);
  }

  @Override
  protected BrokeredIdentityContext validateExternalTokenThroughUserInfo(
      EventBuilder event, String subjectToken, String subjectTokenType) {
    if (subjectTokenType.equals(OAuth2Constants.ACCESS_TOKEN_TYPE)) {
      return super.validateExternalTokenThroughUserInfo(event, subjectToken, subjectTokenType);
    }
    event.detail("validation_method", "user info with id token");
    JsonNode userInfo;
    try {
      GoogleIdToken idToken = googleIdTokenVerifier.verify(subjectToken);
      userInfo = asJsonNode(idToken.getPayload().toString());
    } catch (GeneralSecurityException e) {
      logger.error("Security: Failed to extract identity from token", e);
      event.detail(
          Details.REASON, "Failed to extract identity from token:" + e.getLocalizedMessage());
      event.error(Errors.INVALID_TOKEN);
      throw new ErrorResponseException(
          OAuthErrorException.INVALID_TOKEN, e.getLocalizedMessage(), Response.Status.BAD_REQUEST);
    } catch (IOException e) {
      logger.error("Failed to get identity from Google due to connectivity", e);
      event.detail(Details.REASON, "Failed to get identity from Google due to connectivity");
      event.error(Errors.IDENTITY_PROVIDER_ERROR);
      throw new ErrorResponseException(
          OAuthErrorException.TEMPORARILY_UNAVAILABLE,
          "invalid token",
          Response.Status.SERVICE_UNAVAILABLE);
    } catch (NullPointerException e) {
      logger.error("Failed to extract identity from token", e);
      event.detail(
          Details.REASON, "Failed to extract identity from token:" + e.getLocalizedMessage());
      event.error(Errors.INVALID_TOKEN);
      throw new ErrorResponseException(
          OAuthErrorException.INVALID_TOKEN,
          "Google token verifier validation returned null",
          Response.Status.BAD_REQUEST);
    }
    return extractIdentityFromProfile(null, userInfo);
  }
}
