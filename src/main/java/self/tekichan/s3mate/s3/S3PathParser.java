package self.tekichan.s3mate.s3;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * S3 Path Parser
 * <p>
 *     It parses a S3 path of various representations
 *     and extracts important information defined in S3Path.
 * </p>
 */
public final class S3PathParser {

    private S3PathParser() {}

    private static final Pattern S3_URI =
            Pattern.compile("^s3://([^/]+)(?:/(.*))?$");

    private static final Pattern VIRTUAL_HOSTED =
            Pattern.compile("^([^.]+)\\.s3(?:[.-]([a-z0-9-]+))?\\.amazonaws\\.com$");

    private static final Pattern PATH_STYLE =
            Pattern.compile("^s3[.-]([a-z0-9-]+)?\\.amazonaws\\.com$");

    private static final Pattern WEBSITE =
            Pattern.compile("^([^.]+)\\.s3-website[.-]([a-z0-9-]+)\\.amazonaws\\.com$");

    /**
     * Parse the input S3 Path string and extracts important information
     * @param input     S3 Path string
     * @return          S3Path object with important information
     */
    public static S3Path parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("S3 path must not be empty");
        }

        Matcher s3Matcher = S3_URI.matcher(input);
        if (s3Matcher.matches()) {
            return new S3Path(
                    s3Matcher.group(1),
                    Optional.ofNullable(s3Matcher.group(2)).orElse(""),
                    Optional.empty(),
                    S3EndpointType.API
            );
        }

        URI uri = URI.create(input);
        String host = uri.getHost();
        String path = uri.getPath() == null ? "" : uri.getPath();

        if (host == null) {
            throw new IllegalArgumentException("Invalid S3 URL");
        }

        Matcher website = WEBSITE.matcher(host);
        if (website.matches()) {
            return new S3Path(
                    website.group(1),
                    strip(path),
                    Optional.of(website.group(2)),
                    S3EndpointType.WEBSITE
            );
        }

        Matcher virtual = VIRTUAL_HOSTED.matcher(host);
        if (virtual.matches()) {
            return new S3Path(
                    virtual.group(1),
                    strip(path),
                    Optional.ofNullable(virtual.group(2)),
                    S3EndpointType.API
            );
        }

        Matcher pathStyle = PATH_STYLE.matcher(host);
        if (pathStyle.matches()) {
            String[] parts = strip(path).split("/", 2);
            return new S3Path(
                    parts[0],
                    parts.length > 1 ? parts[1] : "",
                    Optional.ofNullable(pathStyle.group(1)),
                    S3EndpointType.API
            );
        }

        throw new IllegalArgumentException("Unsupported S3 URL format");
    }

    private static String strip(String s) {
        return s.startsWith("/") ? s.substring(1) : s;
    }
}