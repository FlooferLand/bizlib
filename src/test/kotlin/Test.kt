import com.flooferland.bizlib.formats.RshowFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.*
import java.io.InputStream
import java.nio.file.Files
import kotlin.io.path.Path

class Test : FunSpec({
    context("Read an rshow file from disk") {
        lateinit var rshowFile: InputStream;
        val prop = runCatching { System.getenv("rshowFile") }.getOrNull()
        if (prop == null) {
            error("Test env var not set: rshowFile. Make sure to set this to an rshw file")
        }
        rshowFile = Files.newInputStream(Path(prop))

        test("Parse a sample file") {
            val format = RshowFormat()
            val out = format.read(rshowFile)
            out.signal.size shouldBeGreaterThan 0
            out.audio.size shouldBeGreaterThan 0
            out.video.size shouldBeGreaterThanOrEqualTo 0
        }
    }
})