package com.example.sukrit.ridersappwedriveyou.Firebase;

import com.example.sukrit.ridersappwedriveyou.Models.Token;
import com.example.sukrit.ridersappwedriveyou.Utils.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by sukrit on 25/4/18.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    DatabaseReference dbToken;
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refreshedToken);
    }

    private void updateTokenToServer(String refreshedToken) {
        dbToken = FirebaseDatabase.getInstance().getReference().child(Common.token_tb1);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid()!=null) //if already login, must update token
        {
            dbToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
        }

    }
}
