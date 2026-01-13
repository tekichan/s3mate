package self.tekichan.s3mate.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private Region resolveBucketRegion(String bucket) {
        try (S3Client probe = S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {
            GetBucketLocationResponse r =
                    probe.getBucketLocation(
                            GetBucketLocationRequest.builder()
                                    .bucket(bucket)
                                    .build()
                    );

            String loc = r.locationConstraintAsString();

            // AWS quirk: null or empty means us-east-1
            if (loc == null || loc.isBlank()) {
                return Region.US_EAST_1;
            }

            return Region.of(loc);
        }
        catch(Exception ex) {
            logger.error("Failed retrieving bucket region for {}", bucket, ex);
            return Region.US_EAST_1;
        }
    }

    private S3Client client(S3Path path) {
        Region region = path.region()
                .map(Region::of)
                .orElseGet(() -> resolveBucketRegion(path.bucket()));

        return S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public HeadObjectResponse metadata(S3Path path) {
        return client(path).headObject(
                HeadObjectRequest.builder()
                        .bucket(path.bucket())
                        .key(path.key())
                        .build()
        );
    }

    public void download(
            S3Path path,
            Path target,
            BiConsumer<Long, Long> progress
    ) throws IOException {

        var response = client(path).getObject(
                GetObjectRequest.builder()
                        .bucket(path.bucket())
                        .key(path.key())
                        .build(),
                ResponseTransformer.toInputStream()
        );

        long total = response.response().contentLength();
        long read = 0;

        try (InputStream in = response;
             OutputStream out = Files.newOutputStream(target)) {

            byte[] buffer = new byte[8192];
            int n;

            while ((n = in.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
                read += n;
                progress.accept(read, total);
            }
        }
    }

    public void upload(
            S3Path path,
            Path file,
            BiConsumer<Long, Long> progress
    ) throws IOException {

        long total = Files.size(file);
        long[] sent = {0};

        InputStream in = new FilterInputStream(Files.newInputStream(file)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int n = super.read(b, off, len);
                if (n > 0) {
                    sent[0] += n;
                    progress.accept(sent[0], total);
                }
                return n;
            }
        };

        client(path).putObject(
                PutObjectRequest.builder()
                        .bucket(path.bucket())
                        .key(path.key())
                        .build(),
                RequestBody.fromInputStream(in, total)
        );
    }

}
