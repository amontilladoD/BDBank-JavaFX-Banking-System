package com.bdbank.util;

import com.bdbank.model.Account;
import com.bdbank.model.AdminUser;
import com.bdbank.model.Enums;

/** Simple singleton holding who is currently logged in for this JavaFX session. */
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    public static SessionManager get() { return INSTANCE; }

    private volatile Account currentAccount;
    private volatile AdminUser currentAdmin;
    private volatile Enums.Role role;

    private SessionManager() {}

    public void loginAsUser(Account acc) { this.currentAccount = acc; this.currentAdmin = null; this.role = Enums.Role.USER; }
    public void loginAsAdmin(AdminUser admin) { this.currentAdmin = admin; this.currentAccount = null; this.role = Enums.Role.ADMIN; }
    public void logout() { this.currentAccount = null; this.currentAdmin = null; this.role = null; }

    public Account getCurrentAccount() { return currentAccount; }
    public AdminUser getCurrentAdmin() { return currentAdmin; }
    public Enums.Role getRole() { return role; }
}
