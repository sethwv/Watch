package net.swvn9;

@SuppressWarnings("unused")
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
