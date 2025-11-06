import com.flooferland.bizlib.bits.BitsMap
import com.flooferland.bizlib.formats.RshowFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.Path

class BizmapTest : FunSpec({
    context("Test bizmap") {
        val mapStream = Files.newInputStream(Path("./test/map.bits"))

        test("Bizmap") {
            val map = BitsMap().load(mapStream)

            val sets = map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
            sets shouldBe "faz: bonnie\nrae: beach_bear"

            for ((mapping, bits) in map.bits) {
                mapping shouldNotBe "any"
            }
        }
    }
})