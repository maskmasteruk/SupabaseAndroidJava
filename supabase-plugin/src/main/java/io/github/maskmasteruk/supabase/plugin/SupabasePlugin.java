package io.github.maskmasteruk.supabase.plugin;

import com.android.build.api.dsl.ApplicationExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SupabasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().withId("com.android.application", plugin -> {
            File configFile = project.file("supabase-config.json");

            if (!configFile.exists()) {
                throw new RuntimeException(
                        "supabase-config.json not found.\n\n" +
                                "Place the file in:\n\n" +
                                "app/supabase-config.json\n\n" +
                                "Example:\n\n" +
                                "{\n" +
                                "  \"supabase_url\": \"https://your-project.supabase.co\",\n" +
                                "  \"supabase_publishable_key\": \"your-publishable-key\"\n" +
                                "}"
                );
            }

            try {
                @SuppressWarnings("NewApi") String content = Files.readString(configFile.toPath());

                JSONObject jsonObject = new JSONObject(content);

                String supabaseUrl = jsonObject.getString("supabase_url");
                String supabasePublishableKey = jsonObject.getString("supabase_publishable_key");


                if (supabaseUrl == null || supabaseUrl.isBlank()) {
                    throw new IllegalArgumentException(
                            "supabase_url is missing in supabase-config.json"
                    );
                }

                if (supabasePublishableKey == null || supabasePublishableKey.isBlank()) {
                    throw new IllegalArgumentException(
                            "supabase_publishable_key is missing in supabase-config.json"
                    );
                }

                System.out.println("SUPABASE URL: " + supabaseUrl);
                System.out.println("SUPABASE PUBLISHABLE KEY: " + supabasePublishableKey);

                ApplicationExtension applicationExtension = project.getExtensions().getByType(ApplicationExtension.class);

                applicationExtension.getBuildFeatures().setBuildConfig(true);
                applicationExtension.getDefaultConfig().buildConfigField(
                        "String",
                        "SUPABASE_URL",
                        "\"" + supabaseUrl + "\""
                );
                applicationExtension.getDefaultConfig().buildConfigField(
                        "String",
                        "SUPABASE_PUBLISHABLE_KEY",
                        "\"" + supabasePublishableKey + "\""
                );

                if (jsonObject.has("supabase_secret_key")) {
                    String supabaseSecretKey = jsonObject.getString("supabase_secret_key");
                    if (supabaseSecretKey != null && !supabaseSecretKey.isEmpty()) {
                        System.out.println("SUPABASE SECRET KEY: " + supabaseSecretKey);
                        applicationExtension.getDefaultConfig().buildConfigField(
                                "String",
                                "SUPABASE_SECRET_KEY",
                                "\"" + supabaseSecretKey + "\""
                        );
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to read or parse supabase-config.json", e);
            }
        });
    }
}
