package org.therismos.jaas;

import java.security.Principal;
import java.util.HashMap;

public class UserPrincipal implements Principal, java.io.Serializable {

  private String name;
  private java.util.HashMap<String,String> map;

    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }
  
  public UserPrincipal(String name) {
    super();
    this.name = name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}