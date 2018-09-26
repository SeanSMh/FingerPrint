package com.example.sean.fingerprinttest;

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;

public class FingerprintDialogFragment extends DialogFragment {
    private FingerprintManager fingerprintManager;
    private CancellationSignal mCancellationSignal;
    private Cipher mCipher;
    private LoginActivity loginActivity;
    private TextView errorMsg;


    /*
     * 标识是否是用户主动取消的认证
     * */
    private boolean isSelfCancelled;

    public void setCipher(Cipher cipher) {
        mCipher = cipher;
    }

    @Override
    public void onAttach(Context context) {  //获取Activity的实例
        super.onAttach(context);
        loginActivity = (LoginActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {  //获取FingerprintManager的实例
        super.onCreate(savedInstanceState);
        fingerprintManager = getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }


    /*
    * 加载布局
    * */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        errorMsg = v.findViewById(R.id.error_msg);
        TextView cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                stopListening();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //开始指纹认证监听
        startListening(mCipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        //停止指纹认证
        stopListening();
    }

    private void startListening(Cipher cipher) {
        isSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal,
                0, new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        if(!isSelfCancelled) {
                            errorMsg.setText(errString);
                            if(errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                                Toast.makeText(loginActivity,errString,Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        errorMsg.setText(helpString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        Toast.makeText(loginActivity,"指纹认真成功", Toast.LENGTH_SHORT).show();
                        loginActivity.onAuthenticated();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        errorMsg.setText("指纹认证失败，请再试一次");
                    }
                },null);
    }

    private void stopListening() {
        if(mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            isSelfCancelled = true;
        }
    }

}
