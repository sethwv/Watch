package net.swvn9;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;

class BotYaml {
    private String Token;
    private String Adminrole;
    private String Whitelist;

    String getToken() {
        return Token;
    }
    void setToken(String Token) {
        this.Token = Token;
    }

    String getAdminrole() {
        return Adminrole;
    }
    void setAdminrole(String AdminRole) {
        this.Adminrole = AdminRole;
    }

    String getWhitelist() {
        return Whitelist;
    }
    void setWhitelist(String Whitelist) {
        this.Whitelist = Whitelist;
    }
}
