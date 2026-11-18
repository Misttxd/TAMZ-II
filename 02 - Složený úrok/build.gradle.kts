// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Na tomto Windows se generovaným složkám vrací ReadOnly a Gradle je pak nesmaže.
// Opravujeme pouze výstupy sestavení, nikoli zdrojové soubory.
if (System.getProperty("os.name").startsWith("Windows")) {
    for (directory in file("app/build").walkTopDown()) {
        if (directory.isDirectory) {
            java.nio.file.Files.setAttribute(directory.toPath(), "dos:readonly", false)
        }
    }
}
