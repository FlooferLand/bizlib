import com.flooferland.bizlib.bits.BitsMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.Path

class BizmapTest : FunSpec({
    context("Test bizmap") {
        val map1Stream = Files.newInputStream(Path("./test/map.bits"))
        val map2Stream = Files.newInputStream(Path("./test/map2.bits"))

        test("Bizmap 1") {
            val map = BitsMap().load(map1Stream)

            val sets = map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
            sets shouldBe "faz: bonnie\nrae: beach_bear"
        }
        test("Bizmap 2") {
            val map = BitsMap().load(map2Stream)
            map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
        }
    }
})