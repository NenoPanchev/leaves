package com.example.leaves.model.view;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.service.UserServiceModel;

import java.util.List;

public class UserView {
    private List<String> messages;
    private UserServiceModel user;

    public UserView() {
    }

    public UserServiceModel getUser() {
        return user;
    }

    public UserView setUser(UserServiceModel user) {
        this.user = user;
        return this;
    }

    public List<String> getMessages() {
        return messages;
    }

    public UserView setMessages(List<String> messages) {
        this.messages = messages;
        return this;
    }
}
