package io.github.maskmasteruk.supabase.auth;

import android.content.Context;
import android.net.Uri;

import java.util.Map;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnFactorCreatedCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorChallengeCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorsCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnVerifyCallback;
import io.github.maskmasteruk.supabase.auth.Enums.AuthProviders;
import io.github.maskmasteruk.supabase.auth.Enums.FactorType;
import io.github.maskmasteruk.supabase.auth.Enums.PhoneChannel;
import io.github.maskmasteruk.supabase.auth.Object.FactorChallenge;
import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;

/**
 * The primary entry point for the Supabase Auth SDK.
 * <p>
 * This class provides a singleton instance to access all authentication services including:
 * <ul>
 *     <li>Email and Password authentication</li>
 *     <li>OAuth and ID Token providers</li>
 *     <li>OTP (One-Time Password) via Email and Phone</li>
 *     <li>Magic Link authentication</li>
 *     <li>Session and User management</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Aggregates specialized services (Mail, Phone, Identity, etc.)
 * into a single, easy-to-use API.
 * <p>
 * <b>Thread Safety:</b> The singleton instance is lazily initialized and thread-safe.
 * Method calls that perform network operations are executed on a background thread.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * SupabaseAuth auth = SupabaseAuth.getInstance(context);
 * </pre>
 *
 * @since 1.0.0
 */
public class SupabaseAuth {
    private static volatile SupabaseAuth instance;
    private final Helper helper;

    private final MailService mailService;
    private final PhoneService phoneService;
    private final IdentityProviderService identityProviderService;
    private final EmailPasswordService emailPasswordService;
    private final AuthService authService;
    private final MFAService mfaService;

    /**
     * Initializes the SupabaseAuth instance and its internal services.
     * Automatically checks for an existing session and refreshes it if necessary.
     *
     * @param context The Android application context.
     */
    private SupabaseAuth(Context context) {
        helper = Helper.getInstance(context);
        mailService = MailService.getInstance(context);
        phoneService = PhoneService.getInstance(context);
        identityProviderService = IdentityProviderService.getInstance(context);
        emailPasswordService = EmailPasswordService.getInstance(context);
        authService = AuthService.getInstance(context);
        mfaService = MFAService.getInstance(context);

        helper.checkIfSessionIsValid();
    }

    /**
     * Returns the singleton instance of SupabaseAuth.
     *
     * @param context The Android context.
     * @return The {@link SupabaseAuth} singleton instance.
     */
    public static SupabaseAuth getInstance(Context context) {
        if (instance == null) {
            synchronized (SupabaseAuth.class) {
                if (instance == null) {
                    instance = new SupabaseAuth(context);
                }
            }
        }
        return instance;
    }

    /**
     * Returns the currently authenticated user from the local session.
     *
     * @return The {@link SupabaseUser} if a session exists, otherwise {@code null}.
     */
    public SupabaseUser getCurrentUser() {
        return helper.getCurrentUser();
    }

    /**
     * Handles the callback URI for email update confirmations.
     *
     * @param uri          The URI received from the intent.
     * @param authCallback Callback for success or failure.
     */
    public void handleUpdateEmailCallback(Uri uri, AuthCallback authCallback) {
        emailPasswordService.handleUpdateEmailCallback(uri, authCallback);
    }

    /**
     * Signs up a new user with an email and password.
     *
     * @param email        The user's email address.
     * @param password     The user's password.
     * @param userData     Optional metadata to store with the user.
     * @param authCallback Callback for success or failure.
     */
    public void signUpWithEmailAndPassword(String email, String password, Map<String, Object> userData, AuthCallback authCallback) {
        emailPasswordService.signUpWithEmailAndPassword(email, password, userData, authCallback);
    }

    /**
     * Signs in an existing user with email and password.
     *
     * @param email        The user's email address.
     * @param password     The user's password.
     * @param authCallback Callback for success or failure.
     */
    public void signInWithEmailAndPassword(String email, String password, AuthCallback authCallback) {
        emailPasswordService.signInWithEmailAndPassword(email, password, authCallback);
    }

    /**
     * Resets a user's password using an OTP sent to their email.
     * Requires providing the email, the new password, and the OTP.
     *
     * @param email              The user's email address.
     * @param password           The new password.
     * @param otp                The one-time password received.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resetPasswordWithOtp(String email, String password, String otp, OnCompleteCallback onCompleteCallback) {
        emailPasswordService.resetPasswordWithOtp(email, password, otp, onCompleteCallback);
    }

    /**
     * Resets the password for the current session user using an OTP.
     *
     * @param password           The new password.
     * @param otp                The one-time password received.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resetPasswordWithOtp(String password, String otp, OnCompleteCallback onCompleteCallback) {
        emailPasswordService.resetPasswordWithOtp(password, otp, onCompleteCallback);
    }

    /**
     * Resets the password using a callback URI (typically from a recovery email link).
     *
     * @param uri                The URI containing the recovery session.
     * @param password           The new password.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resetPasswordWithCallback(Uri uri, String password, OnCompleteCallback onCompleteCallback) {
        emailPasswordService.resetPasswordWithCallback(uri, password, onCompleteCallback);
    }

    /**
     * Signs in a user using an ID Token from a supported third-party provider.
     *
     * @param provider     The ID token provider (e.g., Google, Apple).
     * @param idToken      The ID token string.
     * @param authCallback Callback for success or failure.
     */
    public void signInWithIdToken(AuthProviders.IDTokenProviders provider, String idToken, AuthCallback authCallback) {
        identityProviderService.signInWithIdToken(provider, idToken, authCallback);
    }

    /**
     * Generates a sign-in URL for an OAuth provider.
     *
     * @param provider           The OAuth provider (e.g., GitHub, Discord).
     * @param enableCodeVerify   Whether to use PKCE code verification.
     * @param redirectTo         Optional URL to redirect to after successful sign-in.
     * @return The authorization URL to be opened in a browser or custom tab.
     */
    public String getSignInUrlForAuthProvider(AuthProviders.OAuthProviders provider, boolean enableCodeVerify, String redirectTo) {
        return identityProviderService.getSignInUrlForAuthProvider(provider, enableCodeVerify, redirectTo);
    }

    /**
     * Completes the OAuth sign-in flow using the redirected URI.
     *
     * @param uri                 The URI received after OAuth redirection.
     * @param isCodeVerifyEnabled Whether PKCE was enabled for this request.
     * @param authCallback        Callback for success or failure.
     */
    public void signInWithAuthProvider(Uri uri, boolean isCodeVerifyEnabled, AuthCallback authCallback) {
        identityProviderService.signInWithAuthProvider(uri, isCodeVerifyEnabled, authCallback);
    }

    /**
     * Sends an OTP to the specified phone number.
     *
     * @param phone              The phone number in E.164 format.
     * @param channel            The delivery channel (SMS or WhatsApp).
     * @param onCompleteCallback Callback for operation completion.
     */
    public void sendOtpToPhone(String phone, PhoneChannel channel, OnCompleteCallback onCompleteCallback) {
        phoneService.sendOtpToPhone(phone, channel, onCompleteCallback);
    }

    /**
     * Verifies an OTP sent to a phone number.
     *
     * @param phone            The phone number.
     * @param otp              The one-time password to verify.
     * @param onVerifyCallback Callback for verification result.
     */
    public void verifyPhoneOTP(String phone, String otp, OnVerifyCallback onVerifyCallback) {
        phoneService.verifyPhoneOTP(phone, otp, onVerifyCallback);
    }

    /**
     * Resends a confirmation code for a phone number change.
     *
     * @param phone              The new phone number.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resendPhoneChangeConfirmation(String phone, OnCompleteCallback onCompleteCallback) {
        phoneService.resendPhoneChangeConfirmation(phone, onCompleteCallback);
    }

    /**
     * Resends a phone number verification SMS.
     *
     * @param phone              The phone number to verify.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resendPhoneVerification(String phone, OnCompleteCallback onCompleteCallback) {
        phoneService.resendPhoneVerification(phone, onCompleteCallback);
    }

    /**
     * Sends a Magic Link login email to the user.
     *
     * @param email              The user's email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void sendLoginLinkToEmail(String email, String redirectTo, OnCompleteCallback onCompleteCallback) {
        mailService.sendLoginLinkToEmail(email, redirectTo, onCompleteCallback);
    }

    /**
     * Sends a signup link to the user's email.
     *
     * @param email              The user's email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param userData           Optional metadata to store.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void sendSignupLinkToEmail(String email, String redirectTo, Map<String, Object> userData, OnCompleteCallback onCompleteCallback) {
        mailService.sendSignupLinkToEmail(email, redirectTo, userData, onCompleteCallback);
    }

    /**
     * Sends a password reset email to the user.
     *
     * @param email              The user's email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void sendPasswordResetMail(String email, String redirectTo, OnCompleteCallback onCompleteCallback) {
        mailService.sendPasswordResetMail(email, redirectTo, onCompleteCallback);
    }

    /**
     * Sends a verification email for updating the user's email address.
     *
     * @param newEmail           The new email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void sendEmailUpdateVerificationMail(String newEmail, String redirectTo, OnCompleteCallback onCompleteCallback) {
        mailService.sendEmailUpdateVerificationMail(newEmail, redirectTo, onCompleteCallback);
    }

    /**
     * Verifies an OTP sent to an email address.
     *
     * @param email            The user's email.
     * @param otp              The one-time password to verify.
     * @param onVerifyCallback Callback for verification result.
     */
    public void verifyEmailOTP(String email, String otp, OnVerifyCallback onVerifyCallback) {
        mailService.verifyEmailOTP(email, otp, onVerifyCallback);
    }

    /**
     * Resends a signup verification email.
     *
     * @param email              The user's email.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resendSignUpEmail(String email, OnCompleteCallback onCompleteCallback) {
        mailService.resendSignUpEmail(email, onCompleteCallback);
    }

    /**
     * Resends an email change confirmation message.
     *
     * @param email              The new email address.
     * @param onCompleteCallback Callback for operation completion.
     */
    public void resendEmailChangeConfirmation(String email, OnCompleteCallback onCompleteCallback) {
        mailService.resendEmailChangeConfirmation(email, onCompleteCallback);
    }

    /**
     * Updates the user's metadata.
     *
     * @param userData     Map of data to update.
     * @param authCallback Callback for success or failure.
     */
    public void updateUserData(Map<String, Object> userData, AuthCallback authCallback) {
        authService.updateUserData(userData, authCallback);
    }

    /**
     * Logs in the user using a Magic Link URI.
     *
     * @param uri          The URI containing the session data.
     * @param authCallback Callback for success or failure.
     */
    public void loginWithMagicLink(Uri uri, AuthCallback authCallback) {
        authService.loginWithMagicLink(uri, authCallback);
    }

    /**
     * Signs out the current user and clears the local session.
     * Notifies the Supabase server to invalidate the session.
     *
     * @param onCompleteCallback Callback for operation completion.
     */
    public void signOut(OnCompleteCallback onCompleteCallback) {
        authService.signOut(onCompleteCallback);
    }

    /**
     * Signs out the current user and clears the local session without a callback.
     */
    public void signOut() {
        authService.signOut();
    }

    /**
     * Reauthenticates the user using their current session.
     *
     * @param onCompleteCallback Callback for operation completion.
     */
    public void reauthenticate(OnCompleteCallback onCompleteCallback) {
        authService.reauthenticate(onCompleteCallback);
    }

    /**
     * Verifies an MFA challenge using a verification code.
     *
     * @param factorID           The ID of the MFA factor being verified.
     * @param factorChallenge    The challenge object.
     * @param code               The verification code provided by the user.
     * @param onCompleteCallback Callback to handle the result.
     */
    public void verifyChallengeFactor(String factorID, FactorChallenge factorChallenge, String code, OnCompleteCallback onCompleteCallback) {
        mfaService.verifyChallengeFactor(factorID, factorChallenge, code, onCompleteCallback);
    }

    /**
     * Initiates a challenge for an MFA factor.
     *
     * @param factorID                    The ID of the MFA factor to challenge.
     * @param onGetFactorChallengeCallback Callback to handle the resulting {@link FactorChallenge}.
     */
    public void challengeFactor(String factorID, OnGetFactorChallengeCallback onGetFactorChallengeCallback) {
        mfaService.challengeFactor(factorID, onGetFactorChallengeCallback);
    }

    /**
     * Adds a new MFA factor for the current user.
     *
     * @param factorType              The type of factor to add (e.g., TOTP).
     * @param issuer                  The issuer name.
     * @param name                    A user-friendly name for the factor.
     * @param onFactorCreatedCallback Callback to handle the created factor data.
     */
    public void addFactor(FactorType factorType, String issuer, String name, OnFactorCreatedCallback onFactorCreatedCallback) {
        mfaService.addFactor(factorType, issuer, name, onFactorCreatedCallback);
    }

    /**
     * Deletes an MFA factor.
     *
     * @param factorID           The ID of the factor to delete.
     * @param onCompleteCallback Callback to handle the result.
     */
    public void deleteFactor(String factorID, OnCompleteCallback onCompleteCallback) {
        mfaService.deleteFactor(factorID, onCompleteCallback);
    }

    /**
     * Lists all MFA factors enrolled for the current user.
     *
     * @param onGetFactorsCallback Callback to handle the list of factors.
     */
    public void listAllFactors(OnGetFactorsCallback onGetFactorsCallback) {
        mfaService.listAllFactors(onGetFactorsCallback);
    }

}


