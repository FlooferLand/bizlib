import com.flooferland.bizlib.bits.BitsMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldNotBeEmpty

class BizmapTest : FunSpec({
    context("Test bizmap") {
        val maps = Shared.testDir.toFile().listFiles()?.filter { it.extension == "bits" } ?: emptyList()

        maps.forEach { file ->
            test("Test bits file: ${file.name}") {
                val map = BitsMap().load(file.inputStream())
                val result = map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
                result.shouldNotBeEmpty()
            }
        }
    }
})