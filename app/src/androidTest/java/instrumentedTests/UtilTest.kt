package instrumentedTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.developerlife.giphyviewer.shorten
import com.developerlife.giphyviewer.shortenUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.anko.doAsync
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class UtilTest {

  val longUrl =
      "https://en.wikipedia.org/wiki/Cache_replacement_policies#Last_in_first_out_(LIFO)"
  val shortUrl = "http://tinyurl.com/yakt9emb"

  @Test
  fun canCallShortenUrlInAsyncTask() {
    val latch = CountDownLatch(1)
    lateinit var processedUrl: String
    doAsync {
      processedUrl = shortenUrl(longUrl)
      latch.countDown()
    }
    latch.await()
    assertThat(processedUrl).isNotEqualTo(longUrl)
    assertThat(processedUrl).isEqualTo(shortUrl)
  }

  @Test
  fun canCallShortenUrlCoroutines() {
    val latch = CountDownLatch(1)
    lateinit var processedUrl: String
    GlobalScope.launch(Dispatchers.IO) {
      processedUrl = shorten(longUrl)
      latch.countDown()
    }
    latch.await()
    assertThat(processedUrl).isNotEqualTo(longUrl)
    assertThat(processedUrl).isEqualTo(shortUrl)
  }

}
