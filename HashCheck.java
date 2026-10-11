public class HashCheck {
    public static void main(String[] args) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest("admin".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String b64 = java.util.Base64.getEncoder().encodeToString(hash);
        System.out.println(b64);
    }
}
