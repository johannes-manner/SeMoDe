package de.uniba.dsg.serverless.users;

public enum Role {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static boolean isValidRole(String outsideRole) {
        for (Role r : Role.values()) {
            if (r.getRole().equals(outsideRole)) {
                return true;
            }
        }
        return false;
    }
}
