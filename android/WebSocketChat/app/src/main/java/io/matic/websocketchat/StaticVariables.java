package io.matic.websocketchat;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Matic on 05/09/2017.
 */

public class StaticVariables {

    private static String email;
    private static String password;
    private static URI uri;
    private static final String serverIP = "192.168.1.6:8080";
    public static String SALT = "65743534@!!!4323SADSFDdsafdfsfdsjkjhkjhk???";

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        StaticVariables.email = email;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        StaticVariables.password = password;
    }

    public static void setURI(String url) {
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static URI getURI() {
        return uri;
    }

    public static String getServerIP() {
        return serverIP;
    }

    // get image resource based on provided input
    public static int getProfilePic(String avatar) {
        int profilePicResource = R.mipmap.ic_launcher_round;
        if(avatar.equals("image_boy")) {
            profilePicResource = R.mipmap.avatar_boy;
        } else if(avatar.equals("image_girl")) {
            profilePicResource = R.mipmap.avatar_girl;
        } else if(avatar.equals("image_man")) {
            profilePicResource = R.mipmap.avatar_man;
        } else if(avatar.equals("image_woman")) {
            profilePicResource = R.mipmap.avatar_woman;
        }
        return profilePicResource;
    }

    // hash and salt the password
    public static String bytesToHex(String pw) {
        // prepare hash method
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // hash and salt the password to bytes
        byte[] hash = digest.digest((pw + SALT).getBytes(StandardCharsets.UTF_8));
        StringBuffer hexString = new StringBuffer();
        // turn hashed bytes to string
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
