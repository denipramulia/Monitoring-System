package com.ashutosh.database;

import com.ashutosh.model.UserModel;

public interface UserDatabaseListener {
void onSuccess(UserModel userModel);
void onFailed(String message);

}
