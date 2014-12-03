package com.codigoprogramacion.informatemx.API;

/*
 * Created by xymind on 11/09/14.
 */

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface DiputadosAPI {

    //PARA OTRA VERSION...

    @GET("/diputado/")
    Observable<Response> getDiputados();

    @GET("diputado/{id}")
    Observable<Response> getDiputado(@Path("id") String id);

    @GET("diputado/{id}/proposiciones")
    Observable<Response> getProposiciones(@Path("id") String id);

    @GET("diputado/{id}/votaciones")
    Observable<Response> getVotaciones(@Path("id") String id);

    @GET("diputado/{id}/asistencias")
    Observable<Response> getAsistencias(@Path("id") String id);


}
