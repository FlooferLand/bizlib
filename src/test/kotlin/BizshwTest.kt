import com.flooferland.bizlib.RawShowData
import com.flooferland.bizlib.formats.BizshwFormat
import com.flooferland.bizlib.formats.RshowFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.Path

class BizshwTest : FunSpec({
    context("Convert from an rshw") {
        lateinit var rshowData: RawShowData
        beforeTest {
            rshowData = RshowFormat().read(Files.newInputStream(Path("D:\\Animatronics\\Creative Engineering\\Show tapes (RR)\\64th Birthday.rshw")))
        }

        // NOTE: 64th Birthday.rshw is 250 MBs
        // TODO: Convert audio to OGG
        test("Write and read") {
            /*val format = BizshwFormat()
            val data = RawShowData(
                signal = rshowData.signal,
                audio = rshowData.audio,
                video = rshowData.video,
            )

            format.write(Path("./test/excluded/64th Birthday.bizshw"), data)

            val newData = format.readFile(Path("./test/excluded/64th Birthday.bizshw"))
            newData.audio.size shouldBeExactly data.audio.size
            newData.signal.size shouldBeExactly data.signal.size
            newData.video.size shouldBeExactly data.video.size

            newData.audio contentEquals data.audio
            newData.signal contentEquals data.signal
            newData.video contentEquals data.video*/
        }
    }
})