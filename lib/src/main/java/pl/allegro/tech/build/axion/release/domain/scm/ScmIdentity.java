package pl.allegro.tech.build.axion.release.domain.scm;

public class ScmIdentity {

    private final boolean useDefault;

    private final boolean disableAgentSupport;

    private final boolean privateKeyBased;

    private final boolean usernameBased;

    private final String privateKey;

    private final String passPhrase;

    private final String username;

    private final String password;

    public ScmIdentity(
        boolean useDefault,
        boolean disableAgentSupport,
        boolean privateKeyBased,
        boolean usernameBased,
        String privateKey,
        String passPhrase,
        String username,
        String password
    ) {
        this.useDefault = useDefault;
        this.disableAgentSupport = disableAgentSupport;
        this.privateKeyBased = privateKeyBased;
        this.usernameBased = usernameBased;
        this.privateKey = privateKey;
        this.passPhrase = passPhrase;
        this.username = username;
        this.password = password;
    }

    public static ScmIdentity defaultIdentity() {
        return new ScmIdentity(true, false, false, false, null, null, null, null);
    }

    public static ScmIdentity defaultIdentityWithoutAgents() {
        return new ScmIdentity(true, true, false, false, null, null, null, null);
    }

    public static ScmIdentity keyIdentity(String privateKey, String passPhrase) {
        return new ScmIdentity(false, false, true, false, privateKey, passPhrase, null, null);
    }

    public static ScmIdentity usernameIdentity(String username, String password) {
        return new ScmIdentity(false, false, false, true, null, null, username, password);
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public boolean isDisableAgentSupport() {
        return disableAgentSupport;
    }

    public boolean isPrivateKeyBased() {
        return privateKeyBased;
    }

    public boolean isUsernameBased() {
        return usernameBased;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
