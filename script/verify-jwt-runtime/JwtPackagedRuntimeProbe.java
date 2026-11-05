import cn.hutool.jwt.JWTUtil;
import java.lang.reflect.Field;
import java.util.HashSet;
import org.openea.eap.framework.security.config.SecurityProperties;
import org.openea.eap.framework.security.core.util.JwtUtil;

/** Synthetic credentials only; uses exactly the candidate server's packaged runtime libraries. */
public class JwtPackagedRuntimeProbe {
    public static void main(String[] args) throws Exception {
        Field field = JwtUtil.class.getDeclaredField("_SecurityProperties");
        field.setAccessible(true);
        for (boolean legacy : new boolean[]{false, true}) {
            SecurityProperties properties = new SecurityProperties();
            properties.setJwtIssuer(legacy ? "openBPM" : "isolated-review");
            properties.setJwtOldKey("openBPM");
            properties.setJwtSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
            field.set(null, properties);
            if (legacy && JwtUtil.createOldJwtToken(null, "shared-client", 1800, properties) == null) {
                throw new AssertionError("Legacy JJWT signer failed; Hutool fallback cannot count as legacy compatibility");
            }
            HashSet<String> unique = new HashSet<>();
            for (int index = 0; index < 10000; index++) {
                String token = JwtUtil.generateJwtToken(index % 2 == 0 ? null : "", "shared-client", 1800);
                if (!(legacy ? "HS512" : "HS256").equals(JWTUtil.parseToken(token).getHeader("alg"))) {
                    throw new AssertionError("Unexpected signer algorithm or silent fallback");
                }
                if (!JwtUtil.hasJwtId(token) || JwtUtil.isJwtWithoutId(token) || !unique.add(token)) {
                    throw new AssertionError("Packaged signer must issue independent tokens with jti");
                }
            }
            System.out.println("packaged_signer=" + (legacy ? "legacy-jjwt" : "hutool")
                    + " issued=10000 unique=" + unique.size() + " null_and_empty_userKey=true");
        }
    }
}
