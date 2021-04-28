package com.mcuevapps.mutualert.Service;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.mcuevapps.mutualert.common.Constantes;
import com.mcuevapps.mutualert.common.SharedPreferencesManager;
import com.mcuevapps.mutualert.retrofit.response.UserAuthSuccess;

public class Utils {
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void saveDataLogin(UserAuthSuccess data){
        SharedPreferencesManager.setSomeStringValue(Constantes.PREF_TOKEN, data.getToken());
        SharedPreferencesManager.setSomeStringValue(Constantes.PREF_APELLIDOPAT, data.getProfile().getApepat());
        SharedPreferencesManager.setSomeStringValue(Constantes.PREF_APELLIDOMAT, data.getProfile().getApemat());
        SharedPreferencesManager.setSomeStringValue(Constantes.PREF_NOMBRES, data.getProfile().getNombres());
        SharedPreferencesManager.setSomeStringValue(Constantes.PREF_EMAIL, data.getProfile().getEmail());
        SharedPreferencesManager.setSomeStringValue(Constantes.PREF_AVATAR, data.getProfile().getAvatar());
    }

    public static void removeDataLogin(){
        String username = SharedPreferencesManager.getSomeStringValue(Constantes.PREF_USERNAME);
        SharedPreferencesManager.deleteAllValues();
        if ( !TextUtils.isEmpty(username) ) {
            SharedPreferencesManager.setSomeStringValue(Constantes.PREF_USERNAME, username);
        }
    }
}