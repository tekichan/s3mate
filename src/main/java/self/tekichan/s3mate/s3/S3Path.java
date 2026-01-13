package self.tekichan.s3mate.s3;

import java.util.Optional;

public record S3Path(
        String bucket,
        String key,
        Optional<String> region,
        S3EndpointType endpointType
) {}
