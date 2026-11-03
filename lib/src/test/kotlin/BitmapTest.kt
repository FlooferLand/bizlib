import com.flooferland.bizlib.bits.BitsMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.string.shouldNotBeEmpty

class BitmapTest : FunSpec({
    xcontext("General bitmaps") {
        val maps = Shared.testDir.toFile().listFiles()?.filter { it.extension == "bits" } ?: emptyList()

        maps.forEach { file ->
            test("Bits file: ${file.name}") {
                val map = BitsMap().load(file.inputStream())
                val result = map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
                result.shouldNotBeEmpty()
            }
        }
    }

    context("Any") {
        val map = Shared.testDir.toFile().resolve("any.bits")

        test("Any") {
            val map = BitsMap().load(map.inputStream())
            map.bits.shouldContainKey("any")
        }
    }
})