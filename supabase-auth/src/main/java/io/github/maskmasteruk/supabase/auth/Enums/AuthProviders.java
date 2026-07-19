package io.github.maskmasteruk.supabase.auth.Enums;

/**
 * Contains supported authentication providers for Supabase Auth.
 * <p>
 * This class groups together various authentication providers including OAuth providers
 * and ID token providers.
 *
 * @since 1.0.0
 */
public class AuthProviders {
    /**
     * Supported OAuth providers.
     * These providers are used for redirected authentication flows.
     */
    public enum OAuthProviders {
        DISCORD("discord"),
        GITHUB("github"),
        GITLAB("gitlab"),
        KEYCLOAK("keycloak"),
        LINKEDIN("linkedin"),
        LINKEDIN_OIDC("linkedin_oidc"),
        NOTION("notion"),
        SLACK("slack"),
        SLACK_OIDC("slack_oidc"),
        TWITCH("twitch"),
        TWITTER("twitter"), // OAuth 1.0a
        X("x"),             // OAuth 2.0
        WORKOS("workos"),
        ZOOM("zoom"),
        BITBUCKET("bitbucket"),
        SPOTIFY("spotify"),
        FIGMA("figma"),
        FLY("fly");

        private final String value;

        OAuthProviders(String value) {
            this.value = value;
        }

        /**
         * Returns the string identifier for the OAuth provider.
         *
         * @return The provider string.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Supported ID token providers.
     * These providers allow signing in using a token obtained via their native SDKs.
     */
    public enum IDTokenProviders {
        GOOGLE("google"),
        AZURE("azure"),
        APPLE("apple"),
        KAKAO("kakao"),
        FACEBOOK("facebook");

        private final String value;

        IDTokenProviders(String value) {
            this.value = value;
        }

        /**
         * Returns the string identifier for the ID token provider.
         *
         * @return The provider string.
         */
        public String getValue() {
            return value;
        }
    }
}

