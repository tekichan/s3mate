package self.tekichan.s3mate;

/**
 * Mode Flag for Demo purpose
 */
public final class AppMode {
    private AppMode() {}

    /**
     * DEMO mode is on if System Property "s3mate.demo" is true or
     * Environment Variable "S3MATE_DEMO" is true
     */
    public static final boolean DEMO =
            Boolean.getBoolean("s3mate.demo")
                    || "true".equalsIgnoreCase(System.getenv("S3MATE_DEMO"));
}
