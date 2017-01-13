package io.locative.app.network.callback;

import io.locative.app.model.Account;

public interface GetAccountCallback {
    void onSuccess(Account account);
    void onFailure();
}
