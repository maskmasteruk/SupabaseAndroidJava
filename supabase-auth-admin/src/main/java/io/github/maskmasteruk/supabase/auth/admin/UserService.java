package io.github.maskmasteruk.supabase.auth.admin;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.auth.admin.Callback.OnGetUsersCallback;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Service class for administrative user management operations.
 * <p>
 * This service allows performing actions like inviting users, creating users,
 * fetching user details, updating user information, and deleting users
 * using administrative privileges (Service Role Key).
 */
public class UserService {
    private static volatile UserService instance;
    private final Helper helper;


    private UserService(Context context) {
        helper = Helper.getInstance(context);
    }

    /**
     * Returns the singleton instance of {@link UserService}.
     *
     * @param context The application context.
     * @return The {@link UserService} instance.
     */
    public static UserService getInstance(Context context) {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Sends an invitation email to a user.
     *
     * @param email        The email address of the user to invite.
     * @param redirectTo   The URL to redirect the user to after they accept the invite.
     * @param userData     Optional metadata to associate with the user.
     * @param authCallback Callback to handle the response.
     * @throws SupabaseError If the email is null or empty.
     */
    public void inviteUser(String email, String redirectTo, Map<String, Object> userData, AuthCallback authCallback) {
        if (email == null || email.isEmpty()) {
            throw new SupabaseError("Email is required.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.INVITE);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            if (userData != null && !userData.isEmpty()) {
                jsonObjectStringBuilder.append("data", JsonUtils.toJsonObject(userData));
            }
            if (redirectTo != null && !redirectTo.isEmpty()) {
                jsonObjectStringBuilder.append("redirect_to", redirectTo);
            }

            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().postServiceRole(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                JSONObject resultJson = JsonUtils.getJsonObject(result.getResponse());
                SupabaseUser supabaseUser = new SupabaseUser(resultJson);
                authCallback.onSuccess(supabaseUser);
            } else {
                helper.generateError(result, authCallback);
            }
        });
    }

    /**
     * Creates a new user with administrative privileges.
     *
     * @param email         The user's email address.
     * @param phone         The user's phone number.
     * @param password      The user's password.
     * @param emailConfirm  Whether to auto-confirm the user's email.
     * @param phoneConfirm  Whether to auto-confirm the user's phone number.
     * @param userMetadata  Optional user metadata.
     * @param appMetadata   Optional app metadata.
     * @param banDuration   Optional ban duration (e.g., "24h", "none").
     * @param authCallback  Callback to handle the response.
     */
    //    ban duration is like 24h, 1d
    public void createUser(String email, String phone, String password, Boolean emailConfirm, Boolean phoneConfirm, Map<String, Object> userMetadata, Map<String, Object> appMetadata, String banDuration, AuthCallback authCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            if (emailConfirm != null) {
                jsonObjectStringBuilder.append("email_confirm", emailConfirm);
            }
            if (phoneConfirm != null) {
                jsonObjectStringBuilder.append("phone_confirm", phoneConfirm);
            }
            if (phone != null && !phone.isEmpty()) {
                jsonObjectStringBuilder.append("phone", phone);
            }
            if (password != null && !password.isEmpty()) {
                jsonObjectStringBuilder.append("password", password);
            }
            if (banDuration != null && !banDuration.isEmpty()) {
                jsonObjectStringBuilder.append("ban_duration", banDuration);
            }
            if (userMetadata != null && !userMetadata.isEmpty()) {
                jsonObjectStringBuilder.append("user_metadata", JsonUtils.toJsonObject(userMetadata));
            }
            if (appMetadata != null && !appMetadata.isEmpty()) {
                jsonObjectStringBuilder.append("app_metadata", JsonUtils.toJsonObject(appMetadata));
            }

            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().postServiceRole(authUrlBuilder.build(), inputJson);
            if (result.getCode() == HTTP_OK) {
                JSONObject resultJson = JsonUtils.getJsonObject(result.getResponse());
                SupabaseUser supabaseUser = new SupabaseUser(resultJson);
                authCallback.onSuccess(supabaseUser);
            } else {
                helper.generateError(result, authCallback);
            }
        });
    }

    /**
     * Retrieves a paginated list of users.
     *
     * @param page               The page number (starts from 1).
     * @param perPage            The number of users per page.
     * @param onGetUsersCallback Callback to handle the list of users.
     * @throws SupabaseError If the page number is invalid.
     */
    public void getUsers(int page, int perPage, OnGetUsersCallback onGetUsersCallback) {
        if (page <= 0) {
            throw new SupabaseError("Invalid page no. Page no starts from 1");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);

            authUrlBuilder.appendQueryParam("page", String.valueOf(page));
            authUrlBuilder.appendQueryParam("per_page", String.valueOf(perPage));

            Response result = new RequestHandler().getServiceRole(authUrlBuilder.build());
            if (result.getCode() == HTTP_OK) {
                JSONObject resultJson = JsonUtils.getJsonObject(result.getResponse());
                try {
                    List<SupabaseUser> supabaseUsers = new ArrayList<>();
                    JSONArray users = resultJson.getJSONArray("users");
                    for (int i = 0; i < users.length(); i++) {
                        supabaseUsers.add(new SupabaseUser(users.getJSONObject(i)));
                    }
                    onGetUsersCallback.onSuccess(supabaseUsers);
                } catch (JSONException e) {
                    throw new SupabaseError(e);
                }
            } else {
                helper.generateError(result, onGetUsersCallback);
            }
        });
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param userID       The unique ID of the user.
     * @param authCallback Callback to handle the response.
     * @throws SupabaseError If the userID is null or empty.
     */
    public void getUser(String userID, AuthCallback authCallback) {
        if (userID == null || userID.isEmpty()) {
            throw new SupabaseError("userID can't be null");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);
            authUrlBuilder.appendPath(userID);

            Response result = new RequestHandler().getServiceRole(authUrlBuilder.build());
            if (result.getCode() == HTTP_OK) {
                JSONObject resultJson = JsonUtils.getJsonObject(result.getResponse());
                SupabaseUser supabaseUser = new SupabaseUser(resultJson);
                authCallback.onSuccess(supabaseUser);
            } else {
                helper.generateError(result, authCallback);
            }
        });
    }

    /**
     * Updates an existing user's information.
     *
     * @param userID        The ID of the user to update.
     * @param email         New email address.
     * @param phone         New phone number.
     * @param password      New password.
     * @param emailConfirm  Whether to confirm the email.
     * @param phoneConfirm  Whether to confirm the phone number.
     * @param userMetadata  Updated user metadata.
     * @param appMetadata   Updated app metadata.
     * @param banDuration   New ban duration.
     * @param role          New user role.
     * @param disabled      Whether to disable the user.
     * @param authCallback  Callback to handle the response.
     * @throws SupabaseError If the userID is null or empty.
     */
    public void updateUser(String userID, String email, String phone, String password, Boolean emailConfirm, Boolean phoneConfirm, Map<String, Object> userMetadata, Map<String, Object> appMetadata, String banDuration, String role, Boolean disabled, AuthCallback authCallback) {
        if (userID == null || userID.isEmpty()) {
            throw new SupabaseError("userID can't be null");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);
            authUrlBuilder.appendPath(userID);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            if (emailConfirm != null) {
                jsonObjectStringBuilder.append("email_confirm", emailConfirm);
            }
            if (phoneConfirm != null) {
                jsonObjectStringBuilder.append("phone_confirm", phoneConfirm);
            }
            if (disabled != null) {
                jsonObjectStringBuilder.append("disabled", disabled);
            }
            if (phone != null && !phone.isEmpty()) {
                jsonObjectStringBuilder.append("phone", phone);
            }
            if (password != null && !password.isEmpty()) {
                jsonObjectStringBuilder.append("password", password);
            }
            if (banDuration != null && !banDuration.isEmpty()) {
                jsonObjectStringBuilder.append("ban_duration", banDuration);
            }
            if (userMetadata != null && !userMetadata.isEmpty()) {
                jsonObjectStringBuilder.append("user_metadata", JsonUtils.toJsonObject(userMetadata));
            }
            if (appMetadata != null && !appMetadata.isEmpty()) {
                jsonObjectStringBuilder.append("app_metadata", JsonUtils.toJsonObject(appMetadata));
            }
            if (role != null && !role.isEmpty()) {
                jsonObjectStringBuilder.append("role", role);
            }

            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().putServiceRole(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                JSONObject resultJson = JsonUtils.getJsonObject(result.getResponse());
                SupabaseUser supabaseUser = new SupabaseUser(resultJson);
                authCallback.onSuccess(supabaseUser);
            } else {
                helper.generateError(result, authCallback);
            }
        });
    }

    /**
     * Deletes a user.
     *
     * @param userID             The ID of the user to delete.
     * @param softDelete         Whether to perform a soft delete.
     * @param onCompleteCallback Callback to handle the response.
     * @throws SupabaseError If the userID is null or empty.
     */
    public void deleteUser(String userID, boolean softDelete, OnCompleteCallback onCompleteCallback) {
        if (userID == null || userID.isEmpty()) {
            throw new SupabaseError("userID can't be null");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);
            authUrlBuilder.appendPath(userID);


            String inputJson = new JsonUtils.JsonObjectStringBuilder().append("should_soft_delete", softDelete).build();

            Response result = new RequestHandler().deleteServiceRole(authUrlBuilder.build(), inputJson);
            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

}
