package config;

import User.Group;

import java.io.Serializable;

public class GenerateConfigFile implements Serializable {

    private String filename;
    private String username;
    private Group group;


    public GenerateConfigFile(String filename, String username, Group group) {
        this.username = username;
        this.group    = group;
    }

    public void generate() {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
