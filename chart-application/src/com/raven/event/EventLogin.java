package com.raven.event;

import com.raven.model.Model_Login;
import com.raven.model.Model_Register;

public interface EventLogin {

//    public void login(Model_Login data);

    void login(Model_Login data, EventMessage message);

    void register(Model_Register data, EventMessage message);
    void goRegister();
    void goLogin();
}
