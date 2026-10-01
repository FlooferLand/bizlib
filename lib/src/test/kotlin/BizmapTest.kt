import com.flooferland.bizlib.bits.BitsMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.div

class BizmapTest : FunSpec({
    context("Test bizmap") {
        val map1Stream = Files.newInputStream(Shared.testDir / "map.bits")
        val map2Stream = Files.newInputStream(Shared.testDir / "map2.bits")
        val mapOldStream = Files.newInputStream(Shared.testDir / "mapOld.bits")

        test("Bizmap 1") {
            val map = BitsMap().load(map1Stream)

            val sets = map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
            sets shouldBe "faz: bonnie\nrae: beach_bear"
        }
        test("Bizmap 2") {
            val map = BitsMap().load(map2Stream)
            map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
        }
        test("Bizmap Old") {
            val map = BitsMap().load(mapOldStream)
            map.fixture.map { (key, value) -> "$key: $value" }.joinToString("\n")
        }
    }
})