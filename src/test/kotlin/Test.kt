import com.flooferland.bizlib.formats.RshowFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.*
import io.kotest.matchers.shouldBe
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.Path

class Test : FunSpec({
    context("Read an rshow file from disk") {
        lateinit var rshowFile: InputStream;
        val rshowFilePath = runCatching { System.getenv("rshowFile") }.getOrNull() ?: "./test/1 - Mouth.rshw"

        @Suppress("SENSELESS_COMPARISON")
        if (rshowFilePath == null) {
            error("Test env var not set: rshowFile. Make sure to set this to an rshw file")
        }
        rshowFile = Files.newInputStream(Path(rshowFilePath))

        test("Parse a sample file") {
            val format = RshowFormat()
            val out = format.read(rshowFile)
            var count = 0
            fun expect(enabled: Boolean) {
                val expected = if (enabled) 185 else 0
                val value = out.signal[count]
                if (value == expected) {
                    println("$value = $expected")
                } else {
                    println("/!\\ $value != $expected (at $count)")
                }
                value shouldBe expected
                count += 1
            }

            if ("1 - Mouth.rshw" in rshowFilePath) {
                expect(false); expect(true)
                repeat(27) { expect(false) }
                repeat(15) { expect(true); expect(false); }
                repeat(5) { expect(false) }
                repeat(9) { expect(true); expect(false) }
                repeat(6) { expect(false) }
                repeat(9) { expect(true); expect(false); }
                println("Validated $count bits out of ${out.signal.size}!")
            } else {
                println("NOTE: No in-depth test pattern found. Data might not be correct.")
            }

            out.signal.size shouldBeGreaterThan 0
            out.audio.size shouldBeGreaterThan 0
            out.video.size shouldBeGreaterThanOrEqualTo 0
        }
    }
})