import com.flooferland.bizlib.formats.LegacyRshowFormat
import com.flooferland.bizlib.formats.RshowFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.*
import io.kotest.matchers.shouldBe
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.Path

class RshwTest : FunSpec({
    context("Read an rshow file from disk") {
        lateinit var rshowFile: InputStream

        beforeTest {
            rshowFile = Files.newInputStream(Path("./test/1 - Mouth.rshw"))
        }

        test("Parse a sample file") {
            val format = RshowFormat()
            val data = format.read(rshowFile)
            var count = 0
            fun expect(enabled: Boolean) {
                val expected = if (enabled) 185 else 0
                val value = data.signal[count]
                if (value == expected) {
                    println("$value = $expected")
                } else {
                    println("/!\\ $value != $expected (at $count)")
                }
                value shouldBe expected
                count += 1
            }

            expect(false); expect(true)
            repeat(27) { expect(false) }
            repeat(15) { expect(true); expect(false); }
            repeat(5) { expect(false) }
            repeat(9) { expect(true); expect(false) }
            repeat(6) { expect(false) }
            repeat(9) { expect(true); expect(false); }
            println("Validated $count bits out of ${data.signal.size}!")

            data.signal.size shouldBeGreaterThan 0
            data.audio.size shouldBeGreaterThan 0
            data.video.size shouldBeGreaterThanOrEqualTo 0
        }

        val borkedPath = Path("D:\\Animatronics\\Creative Engineering\\Show tapes\\Rubber Biscuit.rshw")
        test("Test borked rshw").config(enabledIf = { Files.exists(borkedPath) }) {
            val format = RshowFormat()
            val data = format.read(Files.newInputStream(borkedPath))
            data.signal.size shouldBeGreaterThan 0
            data.audio.size shouldBeGreaterThan 0
            data.video.size shouldBeGreaterThanOrEqualTo 0
        }

        test("Parse and write out show audio to disk") {
            val format = RshowFormat()
            val data = format.read(rshowFile)
            Files.write(Path("./test/out.wav"), data.audio)
        }
    }
})