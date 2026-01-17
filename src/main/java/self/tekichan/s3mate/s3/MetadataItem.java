package self.tekichan.s3mate.s3;

/**
 * Data structure of Metadata Table
 * @param field     Metadata field name
 * @param value     Metadata field value
 */
public record MetadataItem(String field, String value) {}
