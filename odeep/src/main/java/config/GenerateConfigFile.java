package config;

import User.Group;

import java.io.Serializable;

public class GenerateConfigFile implements Serializable {

    private Group group;


    public GenerateConfigFile(Group group) {
        this.group = group;
    }

    public void generate() {

    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
