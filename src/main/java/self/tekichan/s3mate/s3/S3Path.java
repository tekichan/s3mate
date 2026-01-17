package self.tekichan.s3mate.s3;

import java.util.Optional;

/**
 * Data structure of S3 Path
 * @param bucket            S3 Bucket
 * @param key               S3 Key
 * @param region            S3 Region
 * @param endpointType      S3 Endpoint Type
 */
public record S3Path(
        String bucket,
        String key,
        Optional<String> region,
        S3EndpointType endpointType
) {}
