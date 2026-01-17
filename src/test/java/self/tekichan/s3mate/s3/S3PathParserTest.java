package self.tekichan.s3mate.s3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class S3PathParserTest {

    @Test
    void shouldParseValidS3Uri() {
        var p = S3PathParser.parse("s3://bucket/key");
        assertEquals("bucket", p.bucket());
        assertEquals("key", p.key());
    }

    @Test
    void shouldParseValidVirtualHosted() {
        var p = S3PathParser.parse("https://bucket.s3.amazonaws.com/file.txt");
        assertEquals("bucket", p.bucket());
        assertEquals("file.txt", p.key());
    }

    @Test
    void shouldParseValidPathStyleRegional() {
        var p = S3PathParser.parse("https://s3.eu-west-1.amazonaws.com/bucket/file");
        assertEquals("bucket", p.bucket());
        assertEquals("file", p.key());
        assertEquals("eu-west-1", p.region().orElseThrow());
    }

    @Test
    void shouldParseValidWebsiteEndpoint() {
        var p = S3PathParser.parse(
                "http://bucket.s3-website-us-east-1.amazonaws.com/index.html");
        assertEquals(S3EndpointType.WEBSITE, p.endpointType());
    }

    @Test
    void shouldFailIfInvalidS3Url() {
        assertThrows(IllegalArgumentException.class,
                () -> S3PathParser.parse("https://example.com/file"));
    }
}