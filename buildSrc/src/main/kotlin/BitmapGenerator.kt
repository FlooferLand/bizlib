import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class BitmapGeneratorTask : DefaultTask() {
    @get:InputDirectory
    lateinit var bitmapDir: File

    @get:OutputDirectory
    lateinit var bitmapsGeneratedDir: File

    @TaskAction
    fun generate() {
        val files = bitmapDir.listFiles()
            ?.asSequence().orEmpty()
            .filter { it.name.endsWith(".txt") }
            .map { it.name }
            .sorted()

        for (fileName in files) {
            val file = File(bitmapDir, fileName)
            val lines = file.readLines()

            val keys = mutableSetOf<String>()
            var localBit = 1
            var globalBit = 1
            var bottomDrawer = false
            val out = StringBuilder("{\n")

            for ((i, line) in lines.withIndex()) {
                if (line.isEmpty()) continue

                val split = line.split('\t').toMutableList()
                if (split.size == 1) split.add("")
                split[0] = split[0].trim()
                split[1] = split[1].trim()

                // Counting the bit ID
                if (split[0].isNotEmpty()) {
                    localBit = split[0].toIntOrNull() ?: continue
                    if (localBit < globalBit)
                        bottomDrawer = true
                }
                globalBit = if (bottomDrawer) localBit + 150 else localBit

                var name = split[1].trim().lowercase()
                if (name.isEmpty() || name == "blank" || "n/a" in name) continue

                name = name
                    .replace(" - ", ".")
                    .replace(" ", "_")
                    .replace("/", "_")
                    .replace("_#", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")

                if (name in keys) {
                    println("${file.name}: Name '$name' has a duplicate at $localBit ($globalBit). Ignoring..")
                    continue
                } else {
                    keys.add(name)
                }
                out.append("\t\"$name\": $globalBit")
                if (i != lines.size - 1)
                    out.appendLine(",")
                else
                    out.appendLine()
            }
            out.appendLine("}")

            val outPath = File(bitmapsGeneratedDir, "${file.nameWithoutExtension}.json")
            outPath.writeText(out.toString())
        }
    }
}