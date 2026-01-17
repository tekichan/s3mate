package self.tekichan.s3mate.demo;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;
import self.tekichan.s3mate.s3.MetadataItem;

import java.io.InputStream;
import java.util.List;

/**
 * Medadata Loader for Demo
 */
public final class DemoMetadataLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DemoMetadataLoader() {}

    /**
     * Load demo metadata
     * @return  List of demo metadata values
     */
    public static List<MetadataItem> load() {
        try (InputStream in =
                     DemoMetadataLoader.class
                             .getResourceAsStream("/demo/metadata.json")) {

            if (in == null) {
                throw new IllegalStateException("demo/metadata.json not found");
            }

            CollectionType type =
                    MAPPER.getTypeFactory()
                            .constructCollectionType(List.class, MetadataItem.class);

            return MAPPER.readValue(in, type);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load demo metadata", e);
        }
    }
}
