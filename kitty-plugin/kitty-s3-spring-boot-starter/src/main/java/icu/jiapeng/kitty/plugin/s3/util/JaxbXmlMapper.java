package icu.jiapeng.kitty.plugin.s3.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.io.StringWriter;

public class JaxbXmlMapper {

    /** AWS S3 REST 请求体常用的默认命名空间（与 SDK 生成的 XML 一致）。 */
    public static final String AWS_S3_XML_NAMESPACE = "http://s3.amazonaws.com/doc/2006-03-01/";

    public <T> String writeValueAsString(T object) {
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to serialize object to XML", e);
        }
    }

    public <T> T readValue(String xml, Class<T> clazz) {
        String normalized = normalizeIncomingS3Xml(xml);
        String forBind = stripAwsDefaultNamespace(normalized);
        try {
            return unmarshal(forBind, clazz);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to deserialize XML to object", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(String xml, Class<T> clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }

    /**
     * 去掉 BOM、裁剪空白，兼容 SDK 带默认 xmlns 的文档与手写无命名空间文档。
     */
    static String normalizeIncomingS3Xml(String xml) {
        if (xml == null) {
            return "";
        }
        String s = xml.trim();
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * 移除根元素上常见的 {@value #AWS_S3_XML_NAMESPACE} 声明，便于与无 namespace 的 JAXB 模型绑定。
     */
    static String stripAwsDefaultNamespace(String xml) {
        if (xml == null || xml.isEmpty()) {
            return xml;
        }
        // 兼容空白/换行/单双引号等变体：<Root ... xmlns="http://.../2006-03-01/" ...>
        String regex = "(?is)(<\\w+\\b[^>]*?)\\s+xmlns\\s*=\\s*(['\"])"
                + java.util.regex.Pattern.quote(AWS_S3_XML_NAMESPACE)
                + "\\2([^>]*>)";
        return xml.replaceFirst(regex, "$1$3");
    }
}
