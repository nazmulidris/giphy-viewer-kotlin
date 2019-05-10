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

@RunWith(AndroidJUnit4::class)
class UtilTest {

  @Test
  fun canCallShortenUrlInAsyncTask() {
    val longUrl =
        "https://en.wikipedia.org/wiki/Cache_replacement_policies#Last_in_first_out_(LIFO)"
    doAsync {
      val shortUrl = shortenUrl(longUrl)
      assertThat(shortUrl).isEqualTo("http://tinyurl.com/yakt9emb")
    }
  }

  @Test
  fun canCallShortenUrlCoroutines() {
    val longUrl =
        "https://en.wikipedia.org/wiki/Cache_replacement_policies#Last_in_first_out_(LIFO)"
    GlobalScope.launch(Dispatchers.IO) {
      val shortUrl = shorten(longUrl)
      assertThat(shortUrl).isEqualTo("http://tinyurl.com/yakt9emb")
    }
  }

}
