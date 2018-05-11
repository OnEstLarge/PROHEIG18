package config;

import User.Group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GenerateConfigFile implements Serializable {

    private String filename;
    private String username;
    private List<Group> groupList;


    public GenerateConfigFile(String filename, String username, List<Group> groupList) {
        this.username = username;
        this.groupList = new ArrayList<Group>(groupList);
    }

    public void generate() {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }
}
