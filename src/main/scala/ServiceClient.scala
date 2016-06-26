import com.squareup.okhttp._

/**
  * Created by aleksandar on 6/26/16.
  */
class ServiceClient(url: String) {
  val client = new OkHttpClient();

  def execute(content: String): Int = {
    val body: RequestBody = RequestBody.create(MediaType.parse("application/text"), content)
    val request = new Request.Builder().url(url).post(body).build()
    val response = client.newCall(request).execute();
    return response.code()
  }

}

object ServiceClient {
  def apply(url: String): ServiceClient = {
    new ServiceClient(url)
  }
}

