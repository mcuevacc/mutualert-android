package com.mcuevapps.mutualert.retrofit;

import com.mcuevapps.mutualert.retrofit.request.RequestAlertContact;
import com.mcuevapps.mutualert.retrofit.request.RequestUserSessionFcm;
import com.mcuevapps.mutualert.retrofit.request.RequestUserStateLocation;
import com.mcuevapps.mutualert.retrofit.response.ResponseAlertContact;
import com.mcuevapps.mutualert.retrofit.response.ResponseAlertContactList;
import com.mcuevapps.mutualert.retrofit.response.ResponseAlertEmergencyList;
import com.mcuevapps.mutualert.retrofit.response.ResponseSuccess;
import com.mcuevapps.mutualert.retrofit.response.ResponseUserAuthSuccess;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AuthMutuAlertService {
    /** User Auth **/
    @GET("user/auth/token")
    Call<ResponseUserAuthSuccess> token();

    /** User Session **/
    @POST("user/session/fcm")
    Call<ResponseSuccess> fcmSession(@Body RequestUserSessionFcm requestUserSessionFcm);

    /** User State **/
    @POST("user/state/location")
    Call<ResponseSuccess> sendLocation(@Body RequestUserStateLocation requestUserStateLocation);

    /** Alert Contact **/
    @GET("alert/contact/l")
    Call<ResponseAlertContactList> getAllContacts();

    @GET("alert/contact/r/{idContact}")
    Call<ResponseAlertContact> getContactById(@Path("idContact") int idContact);

    @POST("alert/contact/c")
    Call<ResponseAlertContact> createContact(@Body RequestAlertContact requestAlertContact);

    @PUT("alert/contact/u/{idContact}")
    Call<ResponseSuccess> updateContact(@Path("idContact") int idContact, @Body RequestAlertContact requestAlertContact);

    @DELETE("alert/contact/d/{idContact}")
    Call<ResponseSuccess> deleteContact(@Path("idContact") int idContact);

    /** Alert Emergency **/
    @POST("alert/emergency/start")
    Call<ResponseSuccess> startEmergency(@Body RequestUserStateLocation requestUserStateLocation);

    @POST("alert/emergency/stop")
    Call<ResponseSuccess> stopEmergency();

    @GET("alert/emergencies")
    Call<ResponseAlertEmergencyList> getAlertEmergencies();
}