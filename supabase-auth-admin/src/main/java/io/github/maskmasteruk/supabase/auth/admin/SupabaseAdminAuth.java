package io.github.maskmasteruk.supabase.auth.admin;

import android.content.Context;

import java.util.Map;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorsCallback;
import io.github.maskmasteruk.supabase.auth.admin.Callback.OnGetLinkCallback;
import io.github.maskmasteruk.supabase.auth.admin.Callback.OnGetUsersCallback;
import io.github.maskmasteruk.supabase.auth.admin.Enums.LinkType;

/**
 * Main entry point for Supabase administrative authentication operations.
 * <p>
 * This class provides access to administrative tasks such as user management,
 * link generation, and MFA management. These operations typically require
 * the Service Role Key.
 */
public class SupabaseAdminAuth {
    private static volatile SupabaseAdminAuth instance;
    private final UserService userService;
    private final LinkService linkService;
    private final MFAService mfaService;

    private SupabaseAdminAuth(Context context) {
        userService = UserService.getInstance(context);
        linkService = LinkService.getInstance(context);
        mfaService = MFAService.getInstance(context);
    }

    /**
     * Returns the singleton instance of {@link SupabaseAdminAuth}.
     *
     * @param context The application context.
     * @return The {@link SupabaseAdminAuth} instance.
     */
    public static SupabaseAdminAuth getInstance(Context context) {
        if (instance == null) {
            synchronized (SupabaseAdminAuth.class) {
                if (instance == null) {
                    instance = new SupabaseAdminAuth(context);
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
     */
    public void inviteUser(String email, String redirectTo, Map<String, Object> userData, AuthCallback authCallback) {
        userService.inviteUser(email, redirectTo, userData, authCallback);
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
     * @param banDuration   Optional ban duration.
     * @param authCallback  Callback to handle the response.
     */
    public void createUser(String email, String phone, String password, Boolean emailConfirm, Boolean phoneConfirm, Map<String, Object> userMetadata, Map<String, Object> appMetadata, String banDuration, AuthCallback authCallback) {
        userService.createUser(email, phone, password, emailConfirm, phoneConfirm, userMetadata, appMetadata, banDuration, authCallback);
    }

    /**
     * Retrieves a paginated list of users.
     *
     * @param page               The page number (starts from 1).
     * @param perPage            The number of users per page.
     * @param onGetUsersCallback Callback to handle the list of users.
     */
    public void getUsers(int page, int perPage, OnGetUsersCallback onGetUsersCallback) {
        userService.getUsers(page, perPage, onGetUsersCallback);
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param userID       The unique ID of the user.
     * @param authCallback Callback to handle the response.
     */
    public void getUser(String userID, AuthCallback authCallback) {
        userService.getUser(userID, authCallback);
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
     */
    public void updateUser(String userID, String email, String phone, String password, Boolean emailConfirm, Boolean phoneConfirm, Map<String, Object> userMetadata, Map<String, Object> appMetadata, String banDuration, String role, Boolean disabled, AuthCallback authCallback) {
        userService.updateUser(userID, email, phone, password, emailConfirm, phoneConfirm, userMetadata, appMetadata, banDuration, role, disabled, authCallback);
    }

    /**
     * Deletes a user.
     *
     * @param userID             The ID of the user to delete.
     * @param softDelete         Whether to perform a soft delete.
     * @param onCompleteCallback Callback to handle the response.
     */
    public void deleteUser(String userID, boolean softDelete, OnCompleteCallback onCompleteCallback) {
        userService.deleteUser(userID, softDelete, onCompleteCallback);
    }

    /**
     * Generates a specific type of authentication link.
     *
     * @param type              The type of link to generate.
     * @param email             The user's email address.
     * @param password          Optional password.
     * @param new_email         Optional new email address.
     * @param redirect_to       Optional URL to redirect to.
     * @param userData          Optional user metadata.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateLink(LinkType type, String email, String password, String new_email, String redirect_to, Map<String, Object> userData, OnGetLinkCallback onGetLinkCallback) {
        linkService.generateLink(type, email, password, new_email, redirect_to, userData, onGetLinkCallback);
    }

    /**
     * Generates a signup link.
     *
     * @param email             The user's email address.
     * @param password          The user's password.
     * @param redirectTo        Optional URL to redirect to.
     * @param userData          Optional user metadata.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateSignupLink(String email, String password, String redirectTo, Map<String, Object> userData, OnGetLinkCallback onGetLinkCallback) {
        generateLink(LinkType.SIGNUP, email, password, null, redirectTo, userData, onGetLinkCallback);
    }

    /**
     * Generates a magic link.
     *
     * @param email             The user's email address.
     * @param redirectTo        Optional URL to redirect to.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateMagicLink(String email, String redirectTo, OnGetLinkCallback onGetLinkCallback) {
        generateLink(LinkType.MAGICLINK, email, null, null, redirectTo, null, onGetLinkCallback);
    }

    /**
     * Generates an invitation link.
     *
     * @param email             The user's email address.
     * @param redirectTo        Optional URL to redirect to.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateInviteLink(String email, String redirectTo, OnGetLinkCallback onGetLinkCallback) {
        generateLink(LinkType.INVITE, email, null, null, redirectTo, null, onGetLinkCallback);
    }

    /**
     * Generates a password recovery link.
     *
     * @param email             The user's email address.
     * @param redirectTo        Optional URL to redirect to.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateRecoveryLink(String email, String redirectTo, OnGetLinkCallback onGetLinkCallback) {
        generateLink(LinkType.RECOVERY, email, null, null, redirectTo, null, onGetLinkCallback);
    }

    /**
     * Generates an email change link for the current email.
     *
     * @param email             The current email address.
     * @param new_email         The new email address.
     * @param redirectTo        Optional URL to redirect to.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateEmailChangeCurrentLink(String email, String new_email, String redirectTo, OnGetLinkCallback onGetLinkCallback) {
        generateLink(LinkType.EMAIL_CHANGE_CURRENT, email, null, new_email, redirectTo, null, onGetLinkCallback);
    }

    /**
     * Generates an email change link for the new email.
     *
     * @param email             The current email address.
     * @param new_email         The new email address.
     * @param redirectTo        Optional URL to redirect to.
     * @param onGetLinkCallback Callback to handle the generated link.
     */
    public void generateEmailChangeNewLink(String email, String new_email, String redirectTo, OnGetLinkCallback onGetLinkCallback) {
        generateLink(LinkType.EMAIL_CHANGE_NEW, email, null, new_email, redirectTo, null, onGetLinkCallback);
    }

    /**
     * Retrieves all MFA factors for a specific user.
     *
     * @param userID               The unique ID of the user.
     * @param onGetFactorsCallback Callback to handle the list of MFA factors.
     */
    public void listFactors(String userID, OnGetFactorsCallback onGetFactorsCallback) {
        mfaService.listFactors(userID, onGetFactorsCallback);
    }

    /**
     * Deletes a specific MFA factor for a user.
     *
     * @param userID             The unique ID of the user.
     * @param factorID           The ID of the MFA factor to delete.
     * @param onCompleteCallback Callback to handle the response.
     */
    public void deleteFactor(String userID, String factorID, OnCompleteCallback onCompleteCallback) {
        mfaService.deleteFactor(userID, factorID, onCompleteCallback);
    }



}
