package com.example.leaves.service.filter;

import com.example.leaves.model.entity.PermissionEntity;

import java.util.List;

public class RoleFilter {
    private List<Long> ids;
    private String name;
    private List<String> permissions;

    public RoleFilter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
