package io.github.maskmasteruk.supabase.auth.admin;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import java.util.Map;

import io.github.maskmasteruk.supabase.auth.admin.Callback.OnGetLinkCallback;
import io.github.maskmasteruk.supabase.auth.admin.Enums.LinkType;
import io.github.maskmasteruk.supabase.auth.admin.Object.SupabaseLink;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Service class for generating administrative links.
 * <p>
 * This service provides methods to generate links for various authentication
 * actions such as signup, invitation, magic link, and recovery, which can then
 * be sent manually to users.
 */
public class LinkService {
    private static volatile LinkService instance;
    private final Helper helper;


    private LinkService(Context context) {
        helper = Helper.getInstance(context);
    }

    /**
     * Returns the singleton instance of {@link LinkService}.
     *
     * @param context The application context.
     * @return The {@link LinkService} instance.
     */
    public static LinkService getInstance(Context context) {
        if (instance == null) {
            synchronized (LinkService.class) {
                if (instance == null) {
                    instance = new LinkService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Generates a specific type of authentication link.
     *
     * @param type              The type of link to generate (e.g., magiclink, recovery).
     * @param email             The user's email address.
     * @param password          Optional password (for signup links).
     * @param newEmail          Optional new email address (for email change links).
     * @param redirectTo        Optional URL to redirect to after link is clicked.
     * @param userData          Optional user metadata.
     * @param onGetLinkCallback Callback to handle the generated {@link SupabaseLink}.
     * @throws SupabaseError If the email is null or empty.
     */
    public void generateLink(LinkType type, String email, String password, String newEmail, String redirectTo, Map<String, Object> userData, OnGetLinkCallback onGetLinkCallback) {
        if (email == null || email.isEmpty()) {
            throw new SupabaseError("email can't be null");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.GENERATE_LINK);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder().append("type", type.getValue());
            jsonObjectStringBuilder.append("email", email);
            if (password != null && !password.isEmpty()) {
                jsonObjectStringBuilder.append("password", password);
            }
            if (newEmail != null && !newEmail.isEmpty()) {
                jsonObjectStringBuilder.append("new_email", newEmail);
            }
            if (redirectTo != null && !redirectTo.isEmpty()) {
                jsonObjectStringBuilder.append("redirect_to", redirectTo);
            }
            if (userData != null && !userData.isEmpty()) {
                jsonObjectStringBuilder.append("data", JsonUtils.toJsonObject(userData));
            }

            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().postServiceRole(authUrlBuilder.build(), inputJson);
            if (result.getCode() == HTTP_OK) {
                onGetLinkCallback.onSuccess(new SupabaseLink(JsonUtils.getJsonObject(result.getResponse())));
            } else {
                helper.generateError(result, onGetLinkCallback);
            }
        });
    }
}

