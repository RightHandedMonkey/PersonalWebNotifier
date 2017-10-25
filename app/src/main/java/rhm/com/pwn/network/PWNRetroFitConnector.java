package rhm.com.pwn.network;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by sambo on 9/3/2017.
 */

public class PWNRetroFitConnector {
    private static Map<String, Retrofit> retrofits = new HashMap<>();

    public static Retrofit getInstance(String baseUrl) {
        Retrofit retrofit = retrofits.get(baseUrl);

        if ( retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public interface GetPage {
        @GET
        Call<String> GetPageAsString(@Url String url);
    }
}
