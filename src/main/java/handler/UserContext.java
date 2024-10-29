package handler;

import exceptions.UserExistsException;
import user.User;

public class UserContext {
    private volatile User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (this.user == null) {
            this.user = user;
            return;
        }
        throw new UserExistsException();
    }
}
