package network;


import java.util.List;

import model.AgeSet;
import model.Gender;
import model.Locations;
import retrofit2.Call;
import retrofit2.http.GET;

public interface APIInterface {

	@GET("getgender.php")
	Call<List<Gender>> getGender();

	@GET("getlocation.php?cities")
	Call<List<Locations>> getLocations();

	@GET("getageset.php")
	Call<List<AgeSet>> getAgeSet();

//
//    @GET
//    Call<ProductsData> getProductsPage(@Url String url);

}
