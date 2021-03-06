package com.mcuevapps.mutualert.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mcuevapps.mutualert.R;
import com.mcuevapps.mutualert.Service.UIService;
import com.mcuevapps.mutualert.common.Constantes;
import com.mcuevapps.mutualert.common.InputFilterMinMax;
import com.mcuevapps.mutualert.retrofit.MutuAlertClient;
import com.mcuevapps.mutualert.retrofit.MutuAlertService;
import com.mcuevapps.mutualert.retrofit.request.RequestUserAccountCheckcode;
import com.mcuevapps.mutualert.retrofit.response.ResponseSuccess;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterCodeFragment extends Fragment implements View.OnClickListener, TextWatcher {

    private static final String TAG = RegisterCodeFragment.class.getSimpleName();

    private FirebaseAuth mAuth;

    private View view;

    private boolean isNewUser;
    private String phone;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private int resendTimeLeft;
    private Handler handler;
    private Runnable runnable;

    private EditText editTextCode;
    private Button buttonContinue;
    private Button buttonResend;

    private MutuAlertClient mutuAlertClient;
    private MutuAlertService mutuAlertService;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public static RegisterCodeFragment newInstance(Boolean isNewUser, String phone) {
        RegisterCodeFragment fragment = new RegisterCodeFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constantes.ARG_NEW_USER, isNewUser);
        args.putString(Constantes.ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isNewUser = getArguments().getBoolean(Constantes.ARG_NEW_USER);
            phone = getArguments().getString(Constantes.ARG_PHONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register_code, container, false);

        retrofitInit();
        initUI();
        return view;
    }

    private void retrofitInit() {
        mutuAlertClient = MutuAlertClient.getInstance();
        mutuAlertService = mutuAlertClient.getMutuAlertService();
    }

    private void initUI() {
        resendTimeLeft = 0;

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                editTextCode.setText(credential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        editTextCode = view.findViewById(R.id.editTextCode);
        editTextCode.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "999999")});
        editTextCode.addTextChangedListener(this);
        editTextCode.requestFocus();

        buttonContinue = view.findViewById(R.id.buttonContinue);
        UIService.ButtonDisable(UIService.BUTTON_PRIMARY, buttonContinue);
        buttonContinue.setOnClickListener(this);

        buttonResend = view.findViewById(R.id.buttonResendCode);
        buttonResend.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(MyApp.getContext().INPUT_METHOD_SERVICE);
        //imm.showSoftInput(editTextCode, InputMethodManager.SHOW_IMPLICIT);
        resendCode();
    }

    public void resendCode(){
        if(resendTimeLeft!=0){
            return;
        }
        UIService.ButtonDisable(UIService.BUTTON_RAISED, buttonResend);
        resendTimeLeft = Constantes.CODE_RESEND_TIME;
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if(resendTimeLeft==Constantes.CODE_RESEND_TIME){
                    if(mResendToken!=null){
                        resendVerificationCode();
                    } else {
                        startPhoneNumberVerification();
                    }
                }
                resendTimeLeft--;

                if (resendTimeLeft!=0) {
                    buttonResend.setText(getString(R.string.resend_code)+" ... "+resendTimeLeft);
                }else{
                    buttonResend.setText(getString(R.string.resend_code));
                }
                if(resendTimeLeft!=0){
                    handler.postDelayed(this, 1000);
                } else {
                    UIService.ButtonEnable(UIService.BUTTON_RAISED, buttonResend);
                }
            }
        };
        handler.postDelayed(runnable, 0000);
    }

    private void startPhoneNumberVerification() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(getString(R.string.phone_code)+phone)
                        .setTimeout(Long.valueOf(Constantes.CODE_RESEND_TIME), TimeUnit.SECONDS)
                        .setActivity(getActivity())
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(getString(R.string.phone_code)+phone)
                        .setTimeout(Long.valueOf(Constantes.CODE_RESEND_TIME), TimeUnit.SECONDS)
                        .setActivity(getActivity())
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(mResendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.buttonContinue:
                goToInfo();
                break;
            case R.id.buttonResendCode:
                resendCode();
                break;
        }
    }

    private void goToInfo() {
        RegisterInfoFragment fragment = RegisterInfoFragment.newInstance(isNewUser, phone, editTextCode.getText().toString());
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutRegister, fragment)
                //.addToBackStack(null)
                .commit();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if( charSequence.length() != Constantes.CODE_LENGTH ) {
            UIService.ButtonDisable(UIService.BUTTON_PRIMARY, buttonContinue);
        } else {
            checkCode(charSequence.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable editable) { }

    public void checkCode(String code){
        RequestUserAccountCheckcode requestUserAccountCheckcode = new RequestUserAccountCheckcode(mVerificationId, code, phone);
        Call<ResponseSuccess> call = mutuAlertService.checkCode(requestUserAccountCheckcode);
        call.enqueue(new Callback<ResponseSuccess>() {
            @Override
            public void onResponse(Call<ResponseSuccess> call, Response<ResponseSuccess> response) {
                if( response.isSuccessful() ){
                    UIService.ButtonEnable(UIService.BUTTON_PRIMARY, buttonContinue);
                    buttonResend.setText(getString(R.string.resend_code));
                    if(runnable != null ) handler.removeCallbacks(runnable);
                    UIService.ButtonDisable(UIService.BUTTON_RAISED, buttonResend);
                    //goToInfo();
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess> call, Throwable t) { }
        });
    }
}