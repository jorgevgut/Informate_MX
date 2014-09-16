package com.codigoprogramacion.informatemx.API;

/*
 * Created by xymind on 15/09/14.
 */

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface InegiAPI {

    //endpoint http://www2.inegi.org.mx/servicioindicadores

    @GET("/obtieneValoresHistoricos")
    Observable<Response> getIndicadores(@Query("Indicador")String i,@Query("ubicacionGeografica")String g,
                                        @Query("fechaInicial")String fi, @Query("fechaFinal")String ff);

}
