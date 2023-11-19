# Keycloak Google One-Tap

This extension for [Keycloak](https://www.keycloak.org),
enables `token-exchange` for the `IdToken` supplied
by [Sign in with Google One-Tap SDKs](https://developers.google.com/identity/authentication).

### Google ID-Token

If you're using Google One-Tap to Sign in with Google SDKs for web or native apps then you get
an [id-token](https://auth0.com/docs/secure/tokens/id-tokens). Keycloak doesn't support exchanging this token for an
`accessToken` of it's own using `token-exchange`. This extension provides a Google One-Tap Identity provider which
supports this.

The extension uses the [Google Client API library](https://developers.google.com/api-client-library/java)
to [verify](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token) the authenticity of the token.
The extension also packages the Google Client API library and is supplied as a shadow jar.

## Installation

1. [Deploy](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations)
   by adding jar to `/opt/keycloak/providers/`
2. Restart Keycloak.

### Docker

If you're using docker, you can install the provider by adding it to

```dockerfile
ADD --chown=keycloak:keycloak https://github.com/avatsav/keycloak-google-one-tap/releases/download/{version}/apple-identity-provider-{version}.jar /opt/keycloak/providers/apple-identity-provider-{verison}.jar
```

## Compatibility

The version of this library mirrors the compatible keycloak version, so you can find the right version that suits your
keycloak version in the releases.

> [!NOTE]
> Starting Keycloak `v21` since some additional properties such
> as `Hosted Domain`, `Use userIp param` and `Request refresh token`
> are not displayed in the Admin UI when configuring the extension.

