package dev.avatsav.keycloak;

import com.google.auto.service.AutoService;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.social.google.GoogleIdentityProviderConfig;

@AutoService(SocialIdentityProviderFactory.class)
public class GoogleOneTapIdentityProviderFactory
    extends AbstractIdentityProviderFactory<GoogleOneTapIdentityProvider>
    implements SocialIdentityProviderFactory<GoogleOneTapIdentityProvider> {

  public static final String ID = "google-one-tap";
  public static final String NAME = "Google One-Tap";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public GoogleOneTapIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
    return new GoogleOneTapIdentityProvider(session, new GoogleIdentityProviderConfig(model));
  }

  @Override
  public IdentityProviderModel createConfig() {
    return new GoogleIdentityProviderConfig();
  }
}
