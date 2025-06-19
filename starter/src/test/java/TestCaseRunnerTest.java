import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//import static com.ibm.cuda.CudaError.Assert;


public class TestCaseRunnerTest {

  private final Vertx vertx = Vertx.vertx();
  private HttpClient client;

  @Test
  public void testing() {
    int a = 6 * 5;
    System.out.println(a);
    Assertions.assertEquals(30, a);
  }

  @Test
  public void testRun() {
    client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 2000, "localhost", "/public-key")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(this::publicKeyHandler);
  }

  private void publicKeyHandler(AsyncResult<Buffer> bufferAsyncResult) {
    if (bufferAsyncResult.succeeded()) {
      System.out.println(bufferAsyncResult.result());
      assert equals(bufferAsyncResult.result());
    }
  }
}
