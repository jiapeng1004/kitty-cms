package icu.jiapeng.kitty.plugin.s3.util;

import icu.jiapeng.kitty.plugin.s3.model.request.CompleteMultipartUploadRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JaxbXmlMapperTest {

    @Test
    void readCompleteMultipartUpload_withAwsDefaultNamespace() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <CompleteMultipartUpload xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
                  <Part>
                    <PartNumber>10</PartNumber>
                    <ETag>"abc123"</ETag>
                  </Part>
                  <Part>
                    <PartNumber>2</PartNumber>
                    <ETag>"def456"</ETag>
                  </Part>
                </CompleteMultipartUpload>
                """;

        JaxbXmlMapper mapper = new JaxbXmlMapper();
        CompleteMultipartUploadRequest req = mapper.readValue(xml, CompleteMultipartUploadRequest.class);
        assertNotNull(req.getParts());
        assertEquals(2, req.getParts().size());
        assertEquals(10, req.getParts().get(0).getPartNumber());
        assertEquals("abc123", req.getParts().get(0).getETag().replace("\"", ""));
    }

    @Test
    void readCompleteMultipartUpload_withoutNamespace() {
        String xml = """
                <CompleteMultipartUpload>
                  <Part><PartNumber>1</PartNumber><ETag>"e1"</ETag></Part>
                </CompleteMultipartUpload>
                """;
        JaxbXmlMapper mapper = new JaxbXmlMapper();
        CompleteMultipartUploadRequest req = mapper.readValue(xml, CompleteMultipartUploadRequest.class);
        assertEquals(1, req.getParts().size());
        assertEquals(1, req.getParts().get(0).getPartNumber());
    }
}
