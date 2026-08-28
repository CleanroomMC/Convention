package com.cleanroommc.conventions;

import java.util.concurrent.Callable;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.authentication.http.BasicAuthentication;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

/**
 * Publishing Conventions plugin.
 */
public class ConventionsPublishingPlugin implements Plugin<Project> {

    private static final String MAVEN_REPOSITORY_NAME = "Cleanroom";
    private static final String MAVEN_REPOSITORY_URL = "https://maven.cleanroommc.com";
    private static final String MAVEN_PUBLICATION = "maven";
    private static final String JAVA_GRADLE_PLUGIN_ID = "java-gradle-plugin";
    private static final String PLUGIN_PUBLISH_ID = "com.gradle.plugin-publish";
    private static final String PUBLISH_PLUGINS_TASK = "publishPlugins";

    @Override
    public void apply(Project project) {
        PluginManager plugins = project.getPluginManager();

        ConventionsFile.CLIFF.unpack(project.getRootProject());

        project.getPlugins().withType(JavaPlugin.class, _ -> configureJavaPublishing(project));
        plugins.withPlugin(JAVA_GRADLE_PLUGIN_ID, _ -> configurePluginPublishing(project));
    }

    private void configureJavaPublishing(Project project) {
        project.getPluginManager().apply(MavenPublishPlugin.class);

        JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
        java.withSourcesJar();
        java.withJavadocJar();

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        addCleanroomRepository(publishing);
        project.afterEvaluate(_ -> {
            if (publishing.getPublications().findByName(MAVEN_PUBLICATION) != null) {
                return;
            }
            publishing.getPublications()
                    .create(MAVEN_PUBLICATION, MavenPublication.class, publication -> publication.from(project.getComponents().getByName("java")));
        });
    }

    private void configurePluginPublishing(Project project) {
        PluginManager plugins = project.getPluginManager();
        plugins.apply(PLUGIN_PUBLISH_ID);
        plugins.apply(SigningPlugin.class);

        addCleanroomRepository(project.getExtensions().getByType(PublishingExtension.class));

        SigningExtension signing = project.getExtensions().getByType(SigningExtension.class);
        signing.setRequired(
                (Callable<Boolean>) () -> project.getGradle()
                        .getTaskGraph()
                        .getAllTasks()
                        .stream()
                        .anyMatch(task -> PUBLISH_PLUGINS_TASK.equals(task.getName()))
        );
        Provider<String> signingKey = project.getProviders().gradleProperty("signingKey");
        Provider<String> signingPassword = project.getProviders().gradleProperty("signingPassword");
        if (signingKey.isPresent() && signingPassword.isPresent()) {
            signing.useInMemoryPgpKeys(signingKey.get(), signingPassword.get());
        }
    }

    private void addCleanroomRepository(PublishingExtension publishing) {
        if (publishing.getRepositories().findByName(MAVEN_REPOSITORY_NAME) != null) {
            return;
        }
        publishing.getRepositories().maven(repository -> {
            repository.setName(MAVEN_REPOSITORY_NAME);
            repository.setUrl(MAVEN_REPOSITORY_URL);
            repository.credentials(PasswordCredentials.class);
            repository.getAuthentication().create("basic", BasicAuthentication.class);
        });
    }

}
