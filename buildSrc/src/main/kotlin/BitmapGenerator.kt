import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import org.gradle.api.file.DirectoryProperty

abstract class BitmapGeneratorTask : DefaultTask() {
    @get:InputDirectory
    abstract val bitmapDir: DirectoryProperty

    @get:OutputDirectory
    abstract val bitmapsGeneratedDir: DirectoryProperty

    @get:OutputDirectory
    abstract val sourceGeneratedDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val inputDir = bitmapDir.get().asFile
        val generatedResDir = bitmapsGeneratedDir.get().asFile
        val kotlinFile = sourceGeneratedDir.get().asFile.resolve("CompiledBitmaps.kt")

        val bitmapFiles = inputDir.listFiles()
            ?.asSequence().orEmpty()
            .filter { it.name.endsWith(".csv") }
            .sortedBy { it.name }
        compileBitmaps(inputDir, generatedResDir, bitmapFiles)
        compileKotlin(kotlinFile, bitmapFiles)
    }

    fun compileKotlin(kotlinFile: File, bitmapFiles: Sequence<File>) {
        val names = bitmapFiles.map { it.nameWithoutExtension }.filter { !it.endsWith("_old") }
        val kotlin = """
            object CompiledBitmaps {
                val ids = hashSetOf(${names.joinToString(", ") { "\"${it}\"" }})
            }
        """.trimIndent()
        kotlinFile.parentFile?.mkdirs()
        kotlinFile.writeText(kotlin)
    }

    fun compileBitmaps(inputDir: File, generatedResDir: File, files: Sequence<File>) {
        for (file in files) {
            val file = File(inputDir, file.name)
            val lines = file.readLines()
            val keys = mutableSetOf<String>()
            var localBit = 1
            var globalBit = 1
            var bottomDrawer = false
            val out = StringBuilder("{\n")

            for ((i, line) in lines.withIndex()) {
                if (line.isEmpty()) continue

                val split = line.split(',').toMutableList()
                if (split.size == 1) split.add("")
                split[0] = split[0].trim()
                split[1] = split[1].trim()

                // Counting the bit ID
                if (split[0].isNotEmpty()) {
                    localBit = split[0].toIntOrNull() ?: error("Bad ID '${split[0]}'")
                    if (localBit < globalBit)
                        bottomDrawer = true
                }
                globalBit = if (bottomDrawer) localBit + 150 else localBit

                val fixture = split[1].trim().lowercase()
                var name = split[2].trim().lowercase()
                if (fixture.isEmpty()) continue
                if (name.isEmpty() || name == "blank" || "n/a" in name) continue

                name = "$fixture.$name"
                    .replace(" ", "_")
                    .replace("/", "_")
                    .replace("+", "and")
                    .replace("_#", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")

                if (name !in keys) keys.add(name)
                out.append("\t\"$name\": $globalBit")
                if (i < lines.size - 1)
                    out.appendLine(",")
                else
                    out.appendLine()
            }
            out.appendLine("}")

            val outPath = File(generatedResDir, "${file.nameWithoutExtension}.json")
            outPath.writeText(out.toString())
        }
    }
}