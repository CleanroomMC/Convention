package com.cleanroommc.conventions;

import javax.inject.Inject;
import me.modmuss50.mpp.ModPublishExtension;
import me.modmuss50.mpp.platforms.curseforge.Curseforge;
import me.modmuss50.mpp.platforms.modrinth.Modrinth;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

public abstract class ConventionsExtension {

    public static final String NAME = "conventions";

    static ConventionsExtension register(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        ConventionsExtension existing = extensions.findByType(ConventionsExtension.class);
        if (existing != null) {
            return existing;
        }
        return extensions.create(NAME, ConventionsExtension.class);
    }

    public ModsExtension getMods() {
        return ((ExtensionAware) this).getExtensions().getByType(ModsExtension.class);
    }

    public void mods(Action<? super ModsExtension> action) {
        action.execute(getMods());
    }

    ModsExtension registerMods() {
        return ((ExtensionAware) this).getExtensions().create(ModsExtension.NAME, ModsExtension.class);
    }

    public abstract static class ModsExtension {

        public static final String NAME = "mods";

        private final Project project;

        @Inject
        public ModsExtension(Project project) {
            this.project = project;
        }

        public Curseforge getCurseforge() {
            return publishMods().curseforge(_ -> {}).get();
        }

        public void setCurseforge(String projectId) {
            getCurseforge().getProjectId().set(projectId);
        }

        public void curseforge(Action<Curseforge> action) {
            publishMods().curseforge(action);
        }

        public Modrinth getModrinth() {
            return publishMods().modrinth(_ -> {}).get();
        }

        public void setModrinth(String projectId) {
            getModrinth().getProjectId().set(projectId);
        }

        public void modrinth(Action<Modrinth> action) {
            publishMods().modrinth(action);
        }

        private ModPublishExtension publishMods() {
            return project.getExtensions().getByType(ModPublishExtension.class);
        }

    }

}
