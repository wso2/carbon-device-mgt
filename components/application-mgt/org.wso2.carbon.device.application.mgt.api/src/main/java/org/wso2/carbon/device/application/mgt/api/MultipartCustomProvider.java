package org.wso2.carbon.device.application.mgt.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.provider.MultipartProvider;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.jaxrs.AnnotationExclusionStrategy;

import javax.mail.Multipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static java.nio.charset.StandardCharsets.UTF_8;

@Provider
@Consumes(MediaType.TEXT_PLAIN)
public class MultipartCustomProvider implements MessageBodyReader<Object> {
    private Gson gson;

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    public Object readFrom(Class<Object> c, Type t, Annotation[] anns, MediaType mt, MultivaluedMap<String,
            String> headers, InputStream inputStream) throws IOException, WebApplicationException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String jsonString = result.toString();

        JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();

        return getGson().fromJson(obj, t);

    }

    private Gson getGson() {
        if (gson == null) {
            final GsonBuilder gsonBuilder = new GsonBuilder()
                    .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                    .setExclusionStrategies(new AnnotationExclusionStrategy());
            gson = gsonBuilder.create();
        }
        return gson;
    }


}


