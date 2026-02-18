import com.flooferland.bizlib.bits.BitsMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.Path

class BizmapTest : FunSpec({
    context("Test bizmap") {
        val mapStream = Files.newInputStream(Path("./test/map.bits"))

        test("Bizmap") {
            val map = BitsMap().load(mapStream)

            val sets = map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
            sets shouldBe "faz: bonnie\nrae: beach_bear"

            /*for ((mapping, bits) in map.bits) {
                mapping shouldNotBe "any"
                for ((bit, data) in bits) {
                    println("$bit: ${data.moves} ${data.rotates}")
                }
            }*/
        }
    }
})